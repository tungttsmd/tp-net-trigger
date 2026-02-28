package tungtt.Pool.Facade;

import tungtt.Pool.Implements.PoolImplement;

public final class PoolFacade {

    private PoolImplement pool;

    public PoolFacade(PoolImplement pool) {
        
        this.pool = pool;
    }

    public void submitJob(String jobName, Runnable job) {
        
        this.pool.submitJob(jobName, job);
    }

    public void shutdown() {
        
        this.pool.shutdown();
    }
}