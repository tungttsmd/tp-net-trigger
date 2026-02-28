package tungtt.Pool.Configs;

import tungtt.Pool.Hooks.WorkerHeartbeatHooker;

public record PoolHeartbeat(
    WorkerHeartbeatHooker workerHeartbeatHooker,
    int heartbeatIntervalSeconds
) {
}
