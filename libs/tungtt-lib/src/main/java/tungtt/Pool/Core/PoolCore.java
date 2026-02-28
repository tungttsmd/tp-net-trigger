package tungtt.Pool.Core;

import tungtt.Pool.Configs.PoolInit;
import tungtt.Pool.Configs.PoolHook;
import tungtt.Pool.Configs.PoolHeartbeat;

import tungtt.Pool.Implements.PoolImplement;
import tungtt.Pool.Facade.PoolFacade;

public class PoolCore {
    
    private static PoolImplement pool;

    private PoolCore() {
    }

    public static PoolFacade start(
        PoolInit config,
        PoolHook hook,
        PoolHeartbeat heartbeat
    ) {

        pool = new PoolImplement(config, hook, heartbeat);

        return new PoolFacade(pool);
    }
}
    