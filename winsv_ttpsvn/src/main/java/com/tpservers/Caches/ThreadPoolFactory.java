package com.tpservers.Caches;

import com.tpservers.Jobs.WorkerPool;

public class ThreadPoolFactory {

    private static final int WORKER_COUNT = 16;
    private static final String WORKER_PREFIX = "worker:632:";
    /**
     * Hàm khởi tạo threads từ WorkerPool
     */
    public static WorkerPool getThreadPool() {
        WorkerPool pool = new WorkerPool(WORKER_PREFIX, WORKER_COUNT);

        for (int i = 0; i < WORKER_COUNT; i++) {

            // Khởi tạo biến id và number
            int id = i;

            // Đăng ký vào redis lần đầu (để kiểm tra)
            pool.submitJob(WORKER_PREFIX + "test_open_thread_number:" + (id<10?"0"+id:id), () -> {
                System.out.println("Success openning thread " + (id<10?"0"+id:id) +" registered in redis:" + Thread.currentThread().getName());
            });
        };

        return pool;
    }
}
