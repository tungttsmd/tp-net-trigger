package com.tpservers.Jobs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

    // Cài đặt prefix thread name
    private final String prefix;

    // Cài đặt số lượng thread
    private int count = 1;

    // Cài đặt worker map (để heartbeat tái sử dụng chứ không cần load lại)
    private final ConcurrentHashMap<String, Thread> workerMap;

    /**
     * Hàm khởi tạo thread factory
     * 
     * @param prefix
     * @param workerMap
     */
    public NamedThreadFactory(String prefix, ConcurrentHashMap<String, Thread> workerMap) {
        this.prefix = prefix;
        this.workerMap = workerMap;
    }

    /**
     * Hàm tạo thread mới
     *   + Tự set tên thread theo prefix và số lượng thread
     *   + Tạo thread
     *   + Lưu thread vào workerMap (để heartbeat tái sử dụng chứ không cần load lại)
     * 
     * @param callbackRunner
     * @return
     */
    @Override
    public Thread newThread(Runnable callbackRunner) {

        // 1. Đặt tên thread theo prefix và số lượng thread
        String name = prefix + ":" + (count<10 ? "0" : "") + count++;

        // 2. Tạo thread
        Thread thread = new Thread(callbackRunner, name);

        // 3. Lưu thread vào workerMap
        workerMap.put(name, thread);   

        // 4. Trả về instance thread vừa tạo
        return thread;
    }
}
