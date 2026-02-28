package com.tpservers.Jobs;

import java.util.UUID;
import java.util.concurrent.*;

import com.tpservers.Caches.RedisServiceManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.github.cdimascio.dotenv.Dotenv;

public class WorkerPool {
    
    // Load biến môi trường
    private static final Dotenv dotenv = Dotenv.load();

    // Cài đặt thread pool
    private final ThreadPoolExecutor pool;

    // Cài đặt Redis
    private final String redisHost = dotenv.get("REDIS_HOST");
    private final String redisPath = dotenv.get("REDIS_PATH");
    private final int redisPort = Integer.parseInt(dotenv.get("REDIS_PORT"));
    
    // Cài đặt Redis pool (có chế độ thread-safe tránh dành port redis)
    private final JedisPool jedisPool = new JedisPool(redisHost, redisPort);
    
    // Cài đặt cấu hình để monitor redis các worker dễ dàng
    private final String prefixThreadSetName;
    private final String threadSetName = dotenv.get("THREAD_SET_NAME");
    private final String busySet;
    private final String idleSet;

    // Biến toàn cục đối tượng để cấu hình số lượng worker cho một công việc
    private final int workerCount; // Cần truyền vào khi tạo đối tượng

    // Đăng ký pool map (tái sử dụng cho heartbeat khi cần mà không cần phải load lại)
    private final ConcurrentHashMap<String, Thread> workerMap = new ConcurrentHashMap<>();

    /**
     * Hàm khởi tạo đối tượng
     * prefixThreadSetName là tên đặt cho nhóm worker pool (ví dụ: worker:ipmi:632)
     * workerCount là số lượng worker trong pool
     * 
     * @param prefixThreadSetName
     * @param workerCount
     */
    public WorkerPool(String prefixThreadSetName, int workerCount) {
        // 1. Khởi tạo biến toàn cục đối tượng
        this.prefixThreadSetName = prefixThreadSetName;
        this.workerCount = workerCount;
        this.busySet = this.prefixThreadSetName + "busy_workers";
        this.idleSet = this.prefixThreadSetName + "idle_workers";

        // 2. Thực thi heartbeat (thread riêng)
        startHeartbeat();

        // 3. Xử lý dịch vụ Redis server (cần luôn sẵn sàng)
        redisServerAutoLaunch();

        // 4. Pool luôn luôn được tạo
        this.pool = new ThreadPoolExecutor(
                this.workerCount,
                this.workerCount,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(this.threadSetName, this.workerMap)
        );
    }

    /**
     * Hàm thực hiện chạy job được đưa vào và xử lý metadata trong redis cache
     * jobName: tên của công việc được đặt khi chạy worker chạy
     * job: một hàm callback xử lí
     * 
     * Hàm này sẽ thực hiện các bước sau:
     * - Phân phối job: submitJob() gọi WorkerPool.pool.submit()
     * - WorkerPool.pool.submit() sẽ chạy ở một thread riêng biệt trong thread pool
     *  + 1. threadBeforeSubmitJob() ghi busy của thread vào redis (cập nhật monitor)
     *  + 2. submitJobCallback() chạy xử lí job (mục đích tách biệt không đụng đến xử lý thread)
     *  + 3. threadAfterSubmitJob() ghi idle của thread vào lại redis (cập nhật monitor)
     * 
     * @param jobName
     * @param job
     */
    public void submitJob(String jobName, Runnable job) {
        // 1. [REDIS & JOB] Khởi tạo metadata cho job
        String jobId = UUID.randomUUID().toString();
        String jobKey = this.prefixThreadSetName + "job:" + jobId;
        
        // 2. [REDIS] Đánh dấu job QUEUED (chưa vào xử lí thread, nên phải đặt ngoài WorkerPool.pool.submit)
        JobCallback.jobQueueBeforeThreadPoolSubmit(jobId, jobName, this.jedisPool);
        
        // 3. [THREAD POOL] Submit job vào thread pool
        this.pool.submit(() -> {
            // 2.1 [THREAD POOL & REDIS] Khởi tạo biến hỗ trợ tạo metadata  
            String threadName = Thread.currentThread().getName();
            String workerName = Thread.currentThread().getName();
            String workerKey = this.prefixThreadSetName + workerName;

            // 2.2 [THREAD POOL & REDIS] Tiền xử lí redis cache metadata cho trạng thái BUSY
            threadBeforeSubmitJob(workerName, workerKey, jobName);

            // 2.3 [CORE RUN JOB] Chạy hàm callback được truyền vào (nơi thực thi các request vào worker pool)
            submitJobCallback(job, jobId, jobName, threadName, this.prefixThreadSetName, jobKey, this.jedisPool);

            // 2.4 [THREAD POOL & REDIS] Tiền xử lí redis cache metadata cho trạng thái IDLE
            threadAfterSubmitJob(workerName, workerKey);
        });
    }

    /**
     * Hàm thực hiện gửi heartbeat cho worker trên một thread riêng biệt
     */
    private void startHeartbeat() {

        // Tạo đối tượng Thread
        Thread heartbeat = new Thread(() -> {
            while (true) {
                try {
                    // Lấy từ file .env
                    int expire_time = Integer.parseInt(dotenv.get("REDIS_IPMI_THREAD_HEARTBEAT_EXPIRE"));
                    int interval_time = Integer.parseInt(dotenv.get("REDIS_IPMI_THREAD_HEARTBEAT_INTERVAL"));
                    
                    // Vòng lặp cập nhật heartbeat cho worker
                    for (String workerName : workerMap.keySet()) {
                        // Tạo key worker   
                        String key = this.prefixThreadSetName + workerName;
                        String busyKey = this.busySet;
                        String idleKey = this.idleSet;
                        
                        // Gửi heartbeat cho worker
                        try (Jedis r = jedisPool.getResource()) {

                            // Ghi heartbeat cho worker
                            r.hset(key, "heartbeat", System.currentTimeMillis() + "");
                            
                            // Luôn giữ worker tồn tại ít nhất expire_time
                            long ttl = r.ttl(key);

                            if (ttl < expire_time) {
                                r.expire(key, expire_time);
                                r.expire(busyKey, expire_time);
                                r.expire(idleKey, expire_time);
                            } 
                        }
                    }

                    // Giảm tải load lại heartbeat
                    Thread.sleep(interval_time);

                } catch (Exception e) {
                    System.err.println("[HEARTBEAT ERROR] " + e.getMessage());
                }
            }
        });

        // Đặt thread này là daemon thread (chết theo khi chương trình đóng)
        heartbeat.setDaemon(true);

        // Chạy thread
        heartbeat.start();
    }

    /**
     * Hàm thực hiện khởi động dịch vụ Redis server tự động
     */
    private boolean redisServerAutoLaunch() {
        // 1. Tạo biến số để thực hiện kết nối tạm thởi
        Jedis redisConnectTemp = null;
        boolean redisAlive = false;

        // 2. Kiểm tra dịch vụ redis
        try {
            // test connection
            redisConnectTemp = new Jedis(redisHost, redisPort, 12000);

            // try ping
            String pong = redisConnectTemp.ping();
            if ("PONG".equals(pong)) {
                redisAlive = true;
                System.out.println("[REDIS] ONLINE");
            }
            redisConnectTemp.close(); // Chống memory leak
        } catch (Exception e) {
            System.out.println("[REDIS] OFFLINE: " + e.getMessage());
            redisConnectTemp.close(); // Chống memory leak
        }

        // 3. Kiểm tra và khởi động redis-server nếu không có (thử lại 3 lần mỗi 2 giấy)
        if (!redisAlive) {

            // Tạo đối tượng RedisService để quản lý dịch vụ redis-server.exe dễ hơn
            System.out.println("[REDIS SERVER] LAUNCHING...");
            RedisServiceManager redisService = new RedisServiceManager();

            // Thử khởi động redis-server.exe 3 lần
            for (int i = 0; i < 3; i++) {

                // Tiến hành kết nối
                System.out.println("[REDIS] Trying to start Redis... attempt " + (i+1));
                redisService.start(redisPath);

                // Thử lại mỗi 2 giây
                try { Thread.sleep(2000); } catch (Exception ignored) {}

                // Kiểm tra kết nối
                try {
                    String pong = redisConnectTemp.ping();
                    if ("PONG".equals(pong)) {
                        redisAlive = true;
                        break;
                    }
                } catch(Exception ignored) {}
            }
        }

        // Trả kết quả
        if (!redisAlive) {
            System.out.println("[REDIS SERVER FAIL] LAUNCH REDIS SERVER FAIL!");
            return false;
        } else {
            System.out.println("[REDIS SERVER] LAUNCH REDIS SERVER SUCCESS!");
            return true;
        }
    }

    /**
     * === Hàm con của submitJob() ===
     * Hàm xử lý, đóng gói thực hiện callback chính của submitJob()
     * 
     * - job: job thực sự cần thực hiện
     * - jobName: tên job dùng để ghi metadata vào redis
     * - jedisPool: đối tượng Redis pool để ghi metadata vào redis
     * - thread: thread hiện tại dùng lấy thông tin ghi metadata vào redis
     */
    private void submitJobCallback(Runnable job, String jobId, String jobName, String threadName, String prefixThreadSetName, String jobKey, JedisPool jedisPool) {
        // Try catch vì đã được xử lý trong JobCallback.jobWrapper()
        JobCallback.jobWrapper(job, jobId, jobName, prefixThreadSetName, jedisPool).run();
    }

    /**
     * === Hàm con của submitJob() ===
     * Hàm thực hiện tiền xử lí redis cache metadata cho trạng thái BUSY
     */
    private void threadBeforeSubmitJob(String workerName, String workerKey, String jobName) {
        try (Jedis jedis = jedisPool.getResource()) {
                
            // Thêm worker vào set busy workers (nếu chưa tồn tại)
            jedis.sadd(this.busySet, workerName);
            
            // Xóa worker khỏi set idle workers
            jedis.srem(this.idleSet, workerName);
            
            // Tiền xử lí redis cache metadata cho trạng thái BUSY
            jedis.hset(workerKey, "status", "BUSY");
            jedis.hset(workerKey, "job", jobName);
            jedis.hset(workerKey, "heartbeat", String.valueOf(System.currentTimeMillis()));
            jedis.hset(workerKey, "last_job_at", String.valueOf(System.currentTimeMillis()));
        
        } catch (Exception e) {
            System.err.println("[REDIS ERROR BUSY] " + e.getMessage());
        }
    }

    /**
     * === Hàm con của submitJob() ===
     * Hàm thực hiện tiền xử lí redis cache metadata cho trạng thái IDLE
     */
    private void threadAfterSubmitJob(String workerName, String workerKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            
            // Thêm worker vào set idle workers (nếu chưa tồn tại)
            jedis.sadd(this.idleSet, workerName);
            
            // Xóa worker khỏi set busy workers (nếu chưa tồn tại)
            jedis.srem(this.busySet, workerName);
            
            // 3.3. Tiền xử lí redis cache metadata cho trạng thái IDLE
            jedis.hset(workerKey, "status", "IDLE");
            jedis.hset(workerKey, "job", "I'm freeing now sir");
            jedis.hset(workerKey, "heartbeat", String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            System.err.println("[REDIS ERROR IDLE] " + e.getMessage());
        }
    }

    /**
     * Hàm thực hiện tắt pool trong logic nếu cần
     */
    public void shutdown() {
        pool.shutdown();
    }

    /**
     * Hàm thực hiện đợi pool tắt trong logic nếu cần
     */
    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        pool.awaitTermination(timeout, unit);
    }
}
