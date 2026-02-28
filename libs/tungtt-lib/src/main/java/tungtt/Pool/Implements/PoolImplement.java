package tungtt.Pool.Implements;

import tungtt.Console.Console;

import tungtt.Pool.Configs.PoolInit;
import tungtt.Pool.Configs.PoolHook;
import tungtt.Pool.Configs.PoolHeartbeat;
import tungtt.Pool.Factory.NamedThreadFactory;
import tungtt.Pool.Hooks.WorkerLifecycleHooker;
import tungtt.Pool.Hooks.JobLifecycleHooker;
import tungtt.Pool.Hooks.WorkerHeartbeatHooker;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PoolImplement {

    private final ExecutorService pool;

    private final ScheduledExecutorService heartbeatScheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeatPool");
            t.setDaemon(true);
            return t;
        });

    private final Set<String> liveWorkers = ConcurrentHashMap.newKeySet();
    
    private final PoolInit config;
    private final PoolHook hook;
    private final PoolHeartbeat heartbeat;

    public PoolImplement(
        PoolInit config,
        PoolHook hook,
        PoolHeartbeat heartbeat
    ) {
        
        if (config == null) {
            throw new IllegalArgumentException("PoolInit is null");
        }

        this.config = config;
        this.hook = hook;
        this.heartbeat = heartbeat;

        WorkerLifecycleHooker workerLifecycle = new WorkerLifecycleHooker() {

            @Override
            public void onWorkerStart(String name) {
                liveWorkers.add(name);
                if (hook.workerLifecycleHooker() != null) {
                    hook.workerLifecycleHooker().onWorkerStart(name);
                }
            }

            @Override
            public void onWorkerStop(String name) {
                liveWorkers.remove(name);
                if (hook.workerLifecycleHooker() != null) {
                    hook.workerLifecycleHooker().onWorkerStop(name);
                }
            }

            @Override
            public void onWorkerError(String name) {
                if (hook.workerLifecycleHooker() != null) {
                    hook.workerLifecycleHooker().onWorkerError(name);
                }
            }
        };

        this.pool = new ThreadPoolExecutor(
                this.config.workerCount(),
                this.config.workerCount(),
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(
                    this.config.namedThreadPrefix(),
                    workerLifecycle
                )
        );

        startHeartbeat();
    }

    public void submitJob(String jobName, Runnable job) {

        if (this.hook.jobLifecycleHooker() != null) {

            this.hook.jobLifecycleHooker().onJobSubmit(jobName);
        }

        pool.execute(() -> {

            String workerName = Thread.currentThread().getName();

            if (this.hook.jobLifecycleHooker() != null) {

                this.hook.jobLifecycleHooker().onJobStart(jobName, workerName);
            }

            try {

                job.run();
                if (this.hook.jobLifecycleHooker() != null) {

                    this.hook.jobLifecycleHooker().onJobSuccess(jobName, workerName);
                }
            } catch (Throwable e) {

                if (this.hook.jobLifecycleHooker() != null) {
                    
                    this.hook.jobLifecycleHooker().onJobError(jobName, workerName, e);
                }
            }
        });
    }

    public void shutdown() {

        this.heartbeatScheduler.shutdownNow();
        this.pool.shutdown();
    }

    private void startHeartbeat() {

        if (this.heartbeat.workerHeartbeatHooker() == null) return;

        heartbeatScheduler.scheduleAtFixedRate(() -> {
            for (String worker : liveWorkers) {
                this.heartbeat.workerHeartbeatHooker().onHeartbeat(worker, Console.now());
            }
        }, 0,
        this.heartbeat.heartbeatIntervalSeconds(),
        TimeUnit.SECONDS);
    }

}
