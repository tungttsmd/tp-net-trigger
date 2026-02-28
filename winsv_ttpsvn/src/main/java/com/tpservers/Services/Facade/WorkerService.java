package com.tpservers.Services.Facade;

import com.tpservers.Core.PoolCore;

public final class WorkerService {

    private WorkerService() {
    }

    private static class Holder {

        static boolean started = false;
        final static int WORKER_COUNT = PoolCore.getWorkerCount();
        final static String WORKER_PREFIX = PoolCore.getRedisWorkerPrefix();
        final static String WORKER_PREFIX_THREAD_NAME = PoolCore.getWorkerPrefixThreadName();

        final static PoolCore POOL = PoolCore.getInstance();
        final static WorkerService INSTANCE = new WorkerService();
    }

    public static WorkerService getInstance() {
        return Holder.INSTANCE;
    }

    public static PoolCore getPool() {
        return Holder.POOL;
    }

    /* ================= LIFECYCLE ================= */

    public static void start() {
        if (Holder.started) {

            ConsoleService.error("Worker service already started");
            return;
        }

        ConsoleService.info("Connecting to worker service...");
        ConsoleService.info("Worker count: " + Holder.WORKER_COUNT);
        ConsoleService.info("Worker prefix: " + Holder.WORKER_PREFIX);
        ConsoleService.info("Worker prefix thread name: " + Holder.WORKER_PREFIX_THREAD_NAME);

        Holder.started = true;
    }

    /* ================= SUBMIT JOB ================= */

    public static void submitJob(String jobName, Runnable job) {

        if (!Holder.started) {

            ConsoleService.error("Worker service not started");
            return;
        }
        PoolCore.submitJob(jobName, job);
    }
    /* ================= GETTERS ================= */

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
