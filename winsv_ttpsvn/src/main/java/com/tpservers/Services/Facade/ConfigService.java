package com.tpservers.Services.Facade;

import tungtt.HardwareProfile.Contexts.OsContext;
import tungtt.HardwareProfile.Modules.WindowsHardwareProfile;
import oshi.SystemInfo;

public final class ConfigService {

    private ConfigService() {
    }

    private static class Holder {

        static final String prop(String key) {
            return System.getProperty(key);
        }

        static final String propReplaceHostId(String key) {
            String v = prop(key);
            if (v == null || v.isBlank()) {
                throw new IllegalStateException("Missing system property: " + key);
            }
            return v.replace(":hostId", String.valueOf(HardwareService.hwRdp()));
        }

        final static String propReplacePlus(String key) {
            return prop(key).replace(":hostId", "+");
        }

        static final int propIntParse(String key) {
            String v = prop(key);
            if (v == null || v.isBlank()) {
                throw new IllegalStateException("Missing system property: " + key);
            }
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new IllegalStateException(
                        "Invalid integer system property: " + key + " = " + v);
            }
        }

        /* =========================== UPDATE =========================== */
        static final String UPDATE_FOLDER = prop("UPDATE_FOLDER");
        static final String UPDATE_RUN_FILE = prop("UPDATE_RUN_FILE");

        /* =========================== ENV INDENTIFY =========================== */

        static final String HOST_VERSION = prop("HOST_VERSION");
        static final String HOST_FROM_PREFIX = prop("HOST_FROM_PREFIX");
        static final int HOST_ID = Integer.parseInt(HardwareService.hwRdp());

        /* ============================ ENV MQTT ============================ */
        static final String MQTT_SERVER_HOST = prop("MQTT_SERVER_HOST");
        static final int MQTT_SERVER_PORT = propIntParse("MQTT_SERVER_PORT");

        static final String CONTROL_TOPIC = propReplaceHostId("MQTT_CONTROL_TOPIC");

        static final String RUNTIME_TOPIC = propReplaceHostId("MQTT_RUNTIME_TOPIC");
        static final String PROFILE_TOPIC = propReplaceHostId("MQTT_PROFILE_TOPIC");
        static final String HEALTH_TOPIC = propReplaceHostId("MQTT_HEALTH_TOPIC");
        static final String SENSOR_TOPIC = propReplaceHostId("MQTT_SENSOR_TOPIC");
        static final String SYSTEM_TOPIC = propReplaceHostId("MQTT_SYSTEM_TOPIC");
        static final String SIGNAL_TOPIC = propReplaceHostId("MQTT_SIGNAL_TOPIC");

        static final String SIGNAL_TARGET_KEY = propReplaceHostId("MQTT_SIGNAL_TARGET_KEY");

        static final String RUNTIME_WILD_TOPIC = propReplacePlus("MQTT_RUNTIME_TOPIC");
        static final String SENSOR_WILD_TOPIC = propReplacePlus("MQTT_SENSOR_TOPIC");
        static final String PROFILE_WILD_TOPIC = propReplacePlus("MQTT_PROFILE_TOPIC");
        static final String SYSTEM_WILD_TOPIC = propReplacePlus("MQTT_SYSTEM_TOPIC");
        static final String HEALTH_WILD_TOPIC = propReplacePlus("MQTT_HEALTH_TOPIC");
        static final String SIGNAL_WILD_TOPIC = propReplacePlus("MQTT_SIGNAL_TOPIC");

        /* ========================= ENV WORKER POOL ========================== */

        static final String POOL_WORKER_PREFIX = prop("POOL_WORKER_PREFIX");
        static final String POOL_NAMED_THREAD_PREFIX = prop("POOL_NAMED_THREAD_PREFIX");
        static final int POOL_WORKER_COUNT = propIntParse("POOL_WORKER_COUNT");

        static final int POOL_THREAD_HEARTBEAT_EXPIRE = propIntParse("POOL_THREAD_HEARTBEAT_EXPIRE");
        static final int POOL_THREAD_HEARTBEAT_INTERVAL = propIntParse("POOL_THREAD_HEARTBEAT_INTERVAL");

        /* ========================= ENV SENSOR WEB SERVER ========================== */

        static final String SENSOR_WEBSERVER_HOST = prop("SENSOR_WEBSERVER_HOST");
        static final String SENSOR_WEBSERVER_RAW_PATH = prop("SENSOR_WEBSERVER_RAW_PATH");

        static final ConfigService INSTANCE = new ConfigService();
    }

    public static ConfigService getInstance() {

        return Holder.INSTANCE;
    }

    /* ========================= UPDATE ========================== */

    public static String UPDATE_FOLDER() {
        return Holder.UPDATE_FOLDER;
    }

    public static String UPDATE_RUN_FILE() {
        return Holder.UPDATE_RUN_FILE;
    }

    /* ========================= ENV IDENTIFY ========================== */

    public static String HOST_VERSION() {
        return Holder.HOST_VERSION;
    }

    public static String HOST_FROM_PREFIX() {
        return Holder.HOST_FROM_PREFIX;
    }

    public static int HOST_ID() {
        return Holder.HOST_ID;
    }

    /* ========================= ENV MQTT ========================== */
    public static String MQTT_BROKER_URL() {
        return "tcp://" + Holder.MQTT_SERVER_HOST + ":" + Holder.MQTT_SERVER_PORT;
    }

    public static String RUNTIME_TOPIC() {
        return Holder.RUNTIME_TOPIC;
    }

    public static String PROFILE_TOPIC() {
        return Holder.PROFILE_TOPIC;
    }

    public static String HEALTH_TOPIC() {
        return Holder.HEALTH_TOPIC;
    }

    public static String SENSOR_TOPIC() {
        return Holder.SENSOR_TOPIC;
    }

    public static String SYSTEM_TOPIC() {
        return Holder.SYSTEM_TOPIC;
    }

    public static String SIGNAL_TOPIC() {
        return Holder.SIGNAL_TOPIC;
    }

    public static String CONTROL_TOPIC() {
        return Holder.CONTROL_TOPIC;
    }

    public static String SIGNAL_TARGET_KEY() {
        return Holder.SIGNAL_TARGET_KEY;
    }

    public static String RUNTIME_WILD_TOPIC() {
        return Holder.RUNTIME_WILD_TOPIC;
    }

    public static String PROFILE_WILD_TOPIC() {
        return Holder.PROFILE_WILD_TOPIC;
    }

    public static String HEALTH_WILD_TOPIC() {
        return Holder.HEALTH_WILD_TOPIC;
    }

    public static String SENSOR_WILD_TOPIC() {
        return Holder.SENSOR_WILD_TOPIC;
    }

    public static String SYSTEM_WILD_TOPIC() {
        return Holder.SYSTEM_WILD_TOPIC;
    }

    public static String SIGNAL_WILD_TOPIC() {
        return Holder.SIGNAL_WILD_TOPIC;
    }

    /* ========================= ENV WORKER POOL ========================== */
    public static int POOL_THREAD_HEARTBEAT_EXPIRE() {
        return Holder.POOL_THREAD_HEARTBEAT_EXPIRE;
    }

    public static int POOL_THREAD_HEARTBEAT_INTERVAL() {
        return Holder.POOL_THREAD_HEARTBEAT_INTERVAL;
    }

    public static String POOL_NAMED_THREAD_PREFIX() {
        return Holder.POOL_NAMED_THREAD_PREFIX;
    }

    public static int POOL_WORKER_COUNT() {
        return Holder.POOL_WORKER_COUNT;
    }

    public static String POOL_WORKER_PREFIX() {
        return Holder.POOL_WORKER_PREFIX;
    }

    /* ========================= ENV SENSOR WEB SERVER ========================== */

    public static String SENSOR_WEBSERVER_HOST() {
        return Holder.SENSOR_WEBSERVER_HOST;
    }

    public static String SENSOR_WEBSERVER_RAW_PATH() {
        return Holder.SENSOR_WEBSERVER_RAW_PATH;
    }
}
