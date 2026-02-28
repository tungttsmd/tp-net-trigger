package tungtt.Pool.Factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import tungtt.Pool.Hooks.WorkerLifecycleHooker;

public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger counter = new AtomicInteger(0);

    private final WorkerLifecycleHooker workerLifecycleHooker;

    private final String namedThreadPrefix;

    public NamedThreadFactory(
        String namedThreadPrefix,
        WorkerLifecycleHooker workerLifecycle
    ) {

        this.namedThreadPrefix = namedThreadPrefix;
        this.workerLifecycleHooker = workerLifecycle;
    }

    @Override
    public Thread newThread(Runnable r) {

        int id = counter.incrementAndGet();
        String name = namedThreadPrefix + "-" + String.format("%02d", id);

        Runnable wrapped = () -> {

            hookSafe(() -> workerLifecycleHooker.onWorkerStart(name));

            try {

                r.run(); // executor worker run-loop
            } finally {

                hookSafe(() -> workerLifecycleHooker.onWorkerStop(name));
            }
        };

        Thread t = new Thread(wrapped, name);
        t.setDaemon(false);
        t.setPriority(Thread.NORM_PRIORITY);

        /* Catch level Thread */
        t.setUncaughtExceptionHandler((th, ex) -> {
            hookSafe(() -> workerLifecycleHooker.onWorkerError(th.getName()));
        });

        return t;
    }

    /* [Nguyên tắc kiến trúc] hooker không được phép kill worker thread (crash) */
    private void hookSafe(Runnable r) {
        try {
            if (r != null) r.run();
        } catch (Exception ignored) {}
    }

}
