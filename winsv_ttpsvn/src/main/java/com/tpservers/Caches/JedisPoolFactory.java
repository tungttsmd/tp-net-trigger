package com.tpservers.Caches;

import redis.clients.jedis.JedisPool;

public class JedisPoolFactory {
    
    public static JedisPool getJedisPool() {
        return new JedisPool("localhost", 6379);
    }
}
