package com.tpservers.Jobs;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.tpservers.Caches.RedisServiceManager;

import io.github.cdimascio.dotenv.Dotenv;

public class JobCallback {
    // Load biến môi trường
    private static final Dotenv dotenv = Dotenv.load();    

    // Biến kiểm soát TTL của job_metadata và job_status key 
    private static final Integer jobQueueExpiredTime = Integer.parseInt(dotenv.get("REDIS_JOB_QUEUED_EXPIRED_TIME"));
    private static final Integer jobExecuteExpiredTime = Integer.parseInt(dotenv.get("REDIS_JOB_EXECUTED_EXPIRED_TIME"));
    private static final Integer jobDoneExpiredTime = Integer.parseInt(dotenv.get("REDIS_JOB_DONE_EXPIRED_TIME"));
    private static final Integer jobFailExpiredTime = Integer.parseInt(dotenv.get("REDIS_JOB_FAILED_EXPIRED_TIME"));
    
    // Prefix của job_metadata và job_status key
    private static final String jobMonitorPrefixSetName = dotenv.get("REDIS_JOB_PREFIX_SET_NAME");
    private static final String apiPort = dotenv.get("API_PORT_IPMI");

    /**
     * Hàm thiết lập monitor khi một job được queue
     * 
     * @param job
     * @param jobId
     * @param jobName
     * @param prefixThreadSetName
     * @param jedisPool
     * @return
     */
    public static Runnable jobWrapper(Runnable job, String jobId, String jobName, String prefixThreadSetName, JedisPool jedisPool) {
        return () -> {
            // Log console trạng thái executing (dùng cho monitor cấp cli JWM)
            consoleLog(jobId, jobName, threadName(), "EXECUTING", "running...");
            
            // Ghi metadata vào redis trạng thái executing
            cacheJobTrackingExecuteState(jobId, jobName, jedisPool);

            // Thực hiện job thực sự
            try {
                job.run();
            } catch (Exception e) {
                // Log console trạng thái fail (dùng cho monitor cấp cli JWM)
                consoleLog(jobId, jobName, threadName(), "FAIL", e.getMessage());
                
                // Ghi metadata vào redis trạng thái fail
                cacheJobTrackingFailState(jobId, jobName,jedisPool);
            }
            
            // Log console trạng thái done (dùng cho monitor cấp cli JWM)
            consoleLog(jobId, jobName, threadName(), "DONE", "done");
            
            // Ghi metadata vào redis trạng thái done
            cacheJobTrackingDoneState(jobId, jobName, jedisPool);
        };  
    }

    // === PRIVATE FUNCTION ===
    
    /**
     * Hàm ghi metadata vào redis trạng thái executing
     * 
     * @param jobName
     * @param jedisPool
     */
    private static void cacheJobTrackingExecuteState(String jobId, String jobName, JedisPool jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {

            // Tạo metadata key
            String metadataKey = metadataKey(jobId, jobMonitorPrefixSetName);
            
            // Ghi status vào redis
            jedis.hset(jobStatusKey(jobId), "status", "EXECUTING");
            
            // Ghi executed_at vào metadata
            jedis.hset(metadataKey, "executed_at", String.valueOf(System.currentTimeMillis()));

            // Khởi tạo tên thread thật vào metadata
            jedis.hset(metadataKey, "thread", threadName());

            // Cập nhật TTL cho jobkey từ queue đưa vào execute cho phép sống tiếp
            RedisServiceManager.hsetExpired(jobExecuteExpiredTime, jedis, jobStatusKey(jobId), metadataKey);

        } catch (Exception e) {
            // Log console
            consoleLog(jobId, jobName, threadName(), "REDIS EXECUTING ERROR", e.getMessage());
        }
    }

    /**
     * Hàm ghi metadata vào redis trạng thái done
     * 
     * @param jobName
     * @param jedisPool
     * @param prefixThreadSetName
     */
    private static void cacheJobTrackingDoneState(String jobId, String jobName, JedisPool jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {

            // Tạo metadata key
            String metadataKey = metadataKey(jobId, jobMonitorPrefixSetName);
            
            // Ghi status vào redis
            jedis.hset(jobStatusKey(jobId), "status", "DONE");
            
            // Ghi done_at vào metadata
            jedis.hset(metadataKey, "done_at", String.valueOf(System.currentTimeMillis()));

            // Cập nhật TTL cho jobkey từ done đưa vào execute cho phép sống tiếp
            RedisServiceManager.hsetExpired(jobDoneExpiredTime, jedis, jobStatusKey(jobId), metadataKey);

        } catch (Exception e) {
            // Log console
            consoleLog(jobId, jobName, threadName(), "REDIS DONE ERROR", e.getMessage());
        }
    }
    
    /**
     * Hàm ghi metadata vào redis trạng thái fail
     * 
     * @param jobName
     * @param jedisPool
     * @param prefixThreadSetName
     */    
    private static void cacheJobTrackingFailState(String jobId, String jobName, JedisPool jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {

            // Tạo metadata key
            String metadataKey = metadataKey(jobId, jobMonitorPrefixSetName);

            // Ghi status vào redis
            jedis.hset(jobStatusKey(jobId), "status", "FAIL");
            
            // Cập nhật failed_at vào metadata
            jedis.hset(metadataKey, "failed_at", String.valueOf(System.currentTimeMillis()));

            // Cập nhật TTL cho jobkey từ fail đưa vào execute cho phép sống tiếp
            RedisServiceManager.hsetExpired(jobFailExpiredTime, jedis, jobStatusKey(jobId), metadataKey);

        } catch (Exception e) {
            // Log console
            consoleLog(jobId, jobName, threadName(), "REDIS FAIL ERROR", e.getMessage());
        }
    }

    /**
     * Hàm log console
     * 
     * @param threadName
     * @param jobName
     * @param timestamp
     * @param status
     */
    private static void consoleLog(String jobId, String jobName, String threadName, String status, String message) {
        System.out.println("[" + threadName + " - " + System.currentTimeMillis() + " - " + jobId + "] [" + status + "] JOB: " + jobName + " - Message: " + message);
    }

    /**
     * Hàm tạo key metadata theo jobId
     * 
     * @param jobId
     * @return
     */
    private static String metadataKey(String jobId, String prefix) {
        return prefix + apiPort + ":jobs_metadata:" + jobId;
    }

    /**
     * Hàm tạo key job theo jobId
     * 
     * @param jobId
     * @return
     */
    private static String jobStatusKey(String jobId) {
        return jobMonitorPrefixSetName + apiPort + ":job_status:" + jobId;
    }

    /**
     * Hàm lấy tên thread đang chạy excuted -> done job
     * 
     * @return
     */
    private static String threadName() {
        return Thread.currentThread().getName();
    }

    // === PUBLIC FUNCTION ===

    /**
     * Class JobCallback
     * 
     * Đây là hàm thực hiện đầu tiên, được sử dụng trong WorkerPool.submitJob()
     * === Hàm con của submitJob() ===
     * Hàm thực hiện tiền xử lí redis cache metadata cho trạng thái QUEUED
     */
    public static void jobQueueBeforeThreadPoolSubmit(String jobId, String jobName,JedisPool jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {

            // Tạo metadata key
            String metadataKey = metadataKey(jobId, jobMonitorPrefixSetName);
            
            // Ghi status vào redis (lúc này thread chưa thay đổi)
            jedis.hset(jobStatusKey(jobId), "status", "QUEUED");
            
            // Ghi metadata vào redis
            jedis.hset(metadataKey, "queued_at", String.valueOf(System.currentTimeMillis()));
            jedis.hset(metadataKey, "executed_at", "none");
            jedis.hset(metadataKey, "done_at", "none");
            jedis.hset(metadataKey, "failed_at", "none");

            // Mặc định khi ở queue, thread = none hiểu là chưa có thread nào nhận job này
            jedis.hset(metadataKey, "thread", "none");

            // Khởi tạo TTL cho key meta
            RedisServiceManager.hsetExpired(jobQueueExpiredTime, jedis, metadataKey, jobStatusKey(jobId));

        } catch (Exception e) {
            // Log console
            consoleLog(jobId, jobName, "none", "REDIS QUEUED ERROR", e.getMessage());
        }   
    }
}
