package com.tpservers.Caches;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Set;
import java.util.Map;  
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

public class RedisServiceManager  {

    // Tạo instance giữ process Redis, hỗ trợ kill bằng logic
    private Process process;   

    /**
     * Hàm khởi động Redis server
     * @param redisPath
     * @return
     */
    public boolean start(String redisPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(redisPath);

            // Không cho log chiếm terminal
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            // Lưu process vào biến instance, không tạo biến local
            this.process = pb.start(); 

            return true;

        } catch (Exception e) {
            System.err.println("[REDIS FAIL] " + e);
            return false;
        }
    }

    /**
     * Hàm tắt Redis server bằng logic code
     */
    public void stop() {
        if (this.process != null && this.process.isAlive()) {
            this.process.destroy();
            System.out.println("[REDIS SHUTDOWN] Killed redis-server.exe");
        }
    }

    /**
     * Hàm kiểm tra Redis server có đang chạy không
     */
    public boolean isRunning() {
        return this.process != null && this.process.isAlive();
    }

    /**
     * Hàm set TTL cho nhiều key trong hset trong một lúc
     */
    public static void hsetExpired(Integer expiredTime, Jedis jedis, String ...hsetKey) {
        for (String key : hsetKey) {
            jedis.expire(key, expiredTime);
        };
    }

    /**
     * Hàm này sẽ tìm kiếm dữ liệu toàn bộ hset có prefixKey:* rồi sẽ lưu dạng json (trimmed) vào bundleKey
     */
    public static String dataBundle(Jedis jedis, String prefixKey, String bundleKey) {
        // 1. Tìm tất cả key có prefix
        Set<String> keys = jedis.keys(prefixKey + "*");
        
        // Kiểm tra nếu không có key nào
        if (keys == null || keys.isEmpty()) {
            return null;  // hoặc return ""; tùy theo logic của bạn
        }

        JsonArray array = new JsonArray();
    
        for (String key : keys) {
    
            String type = jedis.type(key);   // ❗ Lấy type thật của từng key
    
            JsonObject obj = new JsonObject();
            obj.addProperty("key", key);
            obj.addProperty("type", type);
    
            JsonObject fields = new JsonObject();
    
            switch (type) {
    
                case "hash":
                    Map<String, String> map = jedis.hgetAll(key);
                    for (Map.Entry<String, String> e : map.entrySet()) {
                        fields.addProperty(e.getKey(), e.getValue());
                    }
                    obj.add("fields", fields);
                    break;
    
                case "string":
                    obj.addProperty("value", jedis.get(key));
                    break;
    
                case "list":
                    obj.add("values", new Gson().toJsonTree(jedis.lrange(key, 0, -1)));
                    break;
    
                case "set":
                    obj.add("values", new Gson().toJsonTree(jedis.smembers(key)));
                    break;
    
                case "zset":
                    obj.add("values", new Gson().toJsonTree(jedis.zrange(key, 0, -1)));
                    break;
    
                default:
                    obj.addProperty("warning", "Unsupported type or empty");
                    break;
            }
    
            array.add(obj);
        }
    
        String json = new Gson().toJson(array);
    
        return json;
    }
}
