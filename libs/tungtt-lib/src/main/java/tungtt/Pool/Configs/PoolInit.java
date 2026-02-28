package tungtt.Pool.Configs;

public record PoolInit(
    int workerCount,
    String namedThreadPrefix
) {
    public PoolInit {

        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount <= 0");
        }
    }
}
