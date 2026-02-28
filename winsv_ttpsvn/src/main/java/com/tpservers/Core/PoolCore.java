package com.tpservers.Core;

import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.ConsoleService;
import com.tpservers.Services.Facade.LifecycleHookerService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import tungtt.Pool.Factory.NamedThreadFactory;

public final class PoolCore {

    private PoolCore() {
    }

    private static class Holder {

        static final int WORKER_COUNT = ConfigService.POOL_WORKER_COUNT();
        static final String WORKER_PREFIX = ConfigService.POOL_WORKER_PREFIX();
        static final String WORKER_PREFIX_THREAD_NAME = ConfigService.POOL_NAMED_THREAD_PREFIX();

        static final ThreadPoolExecutor POOL = new ThreadPoolExecutor(
                WORKER_COUNT,
                WORKER_COUNT,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(
                    WORKER_PREFIX_THREAD_NAME,
                    LifecycleHookerService.getInstance()
                ));

        static final PoolCore INSTANCE = new PoolCore();
    }

    /* ========================= JOB SUBMIT ========================== */

    public static void submitJob(String jobName, Runnable job) {

        ConsoleService.info("Submitting job: " + jobName);
        Holder.POOL.submit(() -> {
            try {
                job.run();
            } catch (Exception e) {
                ConsoleService.error("WorkerPool: " + e.getMessage());
            }
        });
    }

    /* =========================== GETTER =========================== */

    public static PoolCore getInstance() {
        return Holder.INSTANCE;
    }

    public static int getWorkerCount() {
        return Holder.WORKER_COUNT;
    }

    public static String getWorkerPrefixThreadName() {
        return Holder.WORKER_PREFIX_THREAD_NAME;
    }

    public static String getRedisWorkerPrefix() {
        return Holder.WORKER_PREFIX;
    }

}
