package com.tpservers.Services.Facade;

import tungtt.Console.Console;

import tungtt.Pool.Hooks.WorkerLifecycleHooker;
import tungtt.Pool.Hooks.JobLifecycleHooker;
import tungtt.Pool.Hooks.WorkerHeartbeatHooker;

public final class LifecycleHookerService implements
    WorkerLifecycleHooker,
    JobLifecycleHooker,
    WorkerHeartbeatHooker {

    private LifecycleHookerService() {}

    private static class Holder {

        static final LifecycleHookerService INSTANCE = new LifecycleHookerService();
    }

    public static LifecycleHookerService getInstance() {
        
        return Holder.INSTANCE;
    }

    /* ============ HOOKER NAMEDTHREAD LIFECYCLE ============ */

    @Override
    public void onWorkerStart(String name) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Worker started: " + name);
    }

    @Override
    public void onWorkerStop(String name) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Worker stopped: " + name);
    }

    @Override
    public void onWorkerError(String name) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Worker error: " + name);
    }

    @Override
    public void onJobSubmit(String job) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Job submitted: " + job);
    }

    @Override
    public void onJobStart(String job, String worker) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Job started: " + job + " by " + worker);
    }

    @Override
    public void onJobSuccess(String job, String worker) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Job success: " + job + " by " + worker);
    }

    @Override
    public void onJobError(String job, String worker, Throwable e) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Job error: " + job + " by " + worker);
    }

    @Override
    public void onHeartbeat(String worker, long ts) {
        
        // Exist for abstract, not need on agent/trigger...
        Console.info("Heartbeat: " + worker + " at " + ts);
    }
}