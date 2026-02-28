package tungtt.Pool.Hooks;

public interface WorkerLifecycleHooker {

    void onWorkerStart(String workerName);

    void onWorkerStop(String workerName);
    
    void onWorkerError(String workerName);
}
