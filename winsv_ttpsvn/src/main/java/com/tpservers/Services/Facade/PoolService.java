package com.tpservers.Services.Facade;

import tungtt.Pool.Configs.PoolInit;
import tungtt.Pool.Configs.PoolHook;
import tungtt.Pool.Configs.PoolHeartbeat;

import tungtt.Pool.Core.PoolCore;
import tungtt.Pool.Facade.PoolFacade;
import tungtt.Console.Console;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.tpservers.Services.Facade.ConfigService;

public final class PoolService {

    private PoolService() {
    }

    private static class Holder {

        static boolean started = false;
        static PoolFacade pool;
        static PoolInit config;
        static PoolHook hook;
        static PoolHeartbeat heartbeat;

        final static PoolService INSTANCE = new PoolService();
    }

    public static PoolService getInstance() {
        return Holder.INSTANCE;
    }

    /* ================= LIFECYCLE ================= */

    public static PoolFacade start() {

        if (Holder.started) {

            Console.error("Worker service already started");
            throw new RuntimeException("Worker service already started");
        };

        Holder.config = new PoolInit(
            ConfigService.POOL_WORKER_COUNT(),
            ConfigService.POOL_NAMED_THREAD_PREFIX()
        );
        
        Holder.hook = new PoolHook(
            LifecycleHookerService.getInstance(), // Implemented WorkerLifecycleHooker
            LifecycleHookerService.getInstance() // Implemented JobLifecycleHooker
        );

        Holder.heartbeat = new PoolHeartbeat(
            LifecycleHookerService.getInstance(), // Implemented WorkerHeartbeatHooker
            ConfigService.POOL_THREAD_HEARTBEAT_INTERVAL()
        );

        Holder.pool = PoolCore.start(
            Holder.config,
            Holder.hook,
            Holder.heartbeat
        );

        Holder.started = true;

        return Holder.pool;
    }

    public static void shutdown() {

        Holder.pool.shutdown();
    }

    /* ================= SUBMIT JOB ================= */

    public static void submitJob(String jobName, Runnable job) {

        if (!Holder.started) {

            Console.error("Worker service not started");
            return;
        }

        Holder.pool.submitJob(jobName, job);

        Console.info("Submitted job: " + jobName);
    }

    /* ================= GETTER ================= */

    public static int getWorkerCount() {

        if (!Holder.started) {

            Console.error("Worker service not started");
            return 0;
        }

        return Holder.config.workerCount();
    }
}
