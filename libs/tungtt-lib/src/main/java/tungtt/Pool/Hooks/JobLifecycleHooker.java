package tungtt.Pool.Hooks;

public interface JobLifecycleHooker {

    void onJobSubmit(String jobName);

    void onJobStart(String jobName, String workerName);

    void onJobSuccess(String jobName, String workerName);

    void onJobError(String jobName, String workerName, Throwable e);
}
