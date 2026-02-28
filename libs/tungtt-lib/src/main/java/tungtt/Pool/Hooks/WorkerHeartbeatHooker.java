package tungtt.Pool.Hooks;

public interface WorkerHeartbeatHooker {

    void onHeartbeat(String workerName, long timestamp);
}
