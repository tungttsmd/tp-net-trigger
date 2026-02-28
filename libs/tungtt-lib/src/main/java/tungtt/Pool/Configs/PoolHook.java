package tungtt.Pool.Configs;

import tungtt.Pool.Hooks.WorkerLifecycleHooker;
import tungtt.Pool.Hooks.JobLifecycleHooker;

public record PoolHook(
    WorkerLifecycleHooker workerLifecycleHooker,
    JobLifecycleHooker jobLifecycleHooker
) {}
