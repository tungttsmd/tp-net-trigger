package com.tpservers.Services.Facade;

import com.tpservers.Repositories.MetaRespository;

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
            return v.replace(":hostId", String.valueOf(MetaRespository.hostId()));
        }

        static final String propReplaceHostIdentify(String key) {
            String v = prop(key);
            if (v == null || v.isBlank()) {
                throw new IllegalStateException("Missing system property: " + key);
            }
            String fromPrefix = prop("HOST_FROM_PREFIX") != null ? prop("HOST_FROM_PREFIX")
                    : "nullOn-" + MetaRespository.hostHwid();
            String deviceType = prop("HOST_DEVICE_TYPE") != null ? prop("HOST_DEVICE_TYPE")
                    : "nullOn-" + MetaRespository.hostHwid();
            return v
                    .replace(":hostFromPrefix", fromPrefix)
                    .replace(":hostId", String.valueOf(MetaRespository.hostId()))
                    .replace(":hostHwid", String.valueOf(MetaRespository.hostHwid()))
                    .replace(":hostDeviceType", deviceType);
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

        /* =========================== ENV INDENTIFY =========================== */

        static final String HOST_VERSION = prop("HOST_VERSION");
        static final String HOST_FROM_PREFIX = prop("HOST_FROM_PREFIX");
        static final int HOST_ID = MetaRespository.hostId();

        /* =========================== ENV NODE ONLY (NOT AGENT) =========================== */

        static final String HOST_DEVICE_TYPE = prop("HOST_DEVICE_TYPE");

        /* ============================ ENV MQTT ============================ */

        static final String MQTT_SERVER_HOST = prop("MQTT_SERVER_HOST");
        static final int MQTT_SERVER_PORT = propIntParse("MQTT_SERVER_PORT");

        static final String MQTT_OPT_USERNAME = prop("MQTT_OPT_USERNAME");
        static final String MQTT_OPT_PASSWORD = prop("MQTT_OPT_PASSWORD");

        static final int MQTT_INIT_MAX_FAIL_TO_REBOOT = propIntParse("MQTT_INIT_MAX_FAIL_TO_REBOOT");

        // SUB
        static final String CONTROL_TOPIC = propReplaceHostIdentify("MQTT_CONTROL_TOPIC");

        // PUB
        static final String RUNTIME_TOPIC = propReplaceHostIdentify("MQTT_RUNTIME_TOPIC");
        static final String PROFILE_TOPIC = propReplaceHostIdentify("MQTT_PROFILE_TOPIC");
        static final String HEALTH_TOPIC = propReplaceHostIdentify("MQTT_HEALTH_TOPIC");
        static final String SENSOR_TOPIC = propReplaceHostIdentify("MQTT_SENSOR_TOPIC");
        static final String SYSTEM_TOPIC = propReplaceHostIdentify("MQTT_SYSTEM_TOPIC");
        static final String SIGNAL_TOPIC = propReplaceHostIdentify("MQTT_SIGNAL_TOPIC");
        static final String SIGNAL_TARGET_KEY = propReplaceHostIdentify("MQTT_SIGNAL_TARGET_KEY");

        /* ========================= ENV WORKER POOL ========================== */

        static final String POOL_WORKER_PREFIX = prop("POOL_WORKER_PREFIX");
        static final String POOL_WORKER_PREFIX_THREAD_NAME = prop("POOL_WORKER_PREFIX_THREAD_NAME");
        static final int POOL_WORKER_COUNT = propIntParse("POOL_WORKER_COUNT");

        /* ========================= ENV SENSOR WEB SERVER ========================== */

        static final String SENSOR_WEBSERVER_HOST = prop("SENSOR_WEBSERVER_HOST");
        static final String SENSOR_WEBSERVER_RAW_PATH = prop("SENSOR_WEBSERVER_RAW_PATH");
        static final int SENSOR_MAX_FAIL_TO_REBOOT = propIntParse("SENSOR_MAX_FAIL_TO_REBOOT");

        /* ========================= ENV HEALTH NETWORK ========================== */

        static final int HEALTH_NETWORK_PING_COUNT = propIntParse("HEALTH_NETWORK_PING_COUNT");
        static final String HEALTH_NETWORK_TARGETS = prop("HEALTH_NETWORK_TARGETS");

        /* ========================= ENV WALLPAPER ========================== */

        static final String WALLPAPER_URL = prop("WALLPAPER_URL");

        static final ConfigService INSTANCE = new ConfigService();
    }

    public static ConfigService getInstance() {

        return Holder.INSTANCE;
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

    /* ========================= ENV NODE ONLY (NOT AGENT) ========================== */

    public static String HOST_DEVICE_TYPE() {
        return Holder.HOST_DEVICE_TYPE;
    }

    /* ========================= ENV MQTT ========================== */

    public static String MQTT_SERVER_HOST() {
        return Holder.MQTT_SERVER_HOST;
    }

    public static int MQTT_SERVER_PORT() {
        return Holder.MQTT_SERVER_PORT;
    }

    public static String MQTT_BROKER_URL() {
        return "tcp://" + Holder.MQTT_SERVER_HOST + ":" + Holder.MQTT_SERVER_PORT;
    }

    public static String MQTT_OPT_USERNAME() {
        return Holder.MQTT_OPT_USERNAME;
    }

    public static String MQTT_OPT_PASSWORD() {
        return Holder.MQTT_OPT_PASSWORD;
    }

    public static int MQTT_INIT_MAX_FAIL_TO_REBOOT() {
        return Holder.MQTT_INIT_MAX_FAIL_TO_REBOOT;
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

    /* ========================= ENV WORKER POOL ========================== */

    public static String POOL_WORKER_PREFIX_THREAD_NAME() {
        return Holder.POOL_WORKER_PREFIX_THREAD_NAME;
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

    public static int SENSOR_MAX_FAIL_TO_REBOOT() {
        return Holder.SENSOR_MAX_FAIL_TO_REBOOT;
    }

    /* ========================= ENV HEALTH NETWORK ========================== */

    public static int HEALTH_NETWORK_PING_COUNT() {
        return Holder.HEALTH_NETWORK_PING_COUNT;
    }

    public static String HEALTH_NETWORK_TARGETS() {
        return Holder.HEALTH_NETWORK_TARGETS;
    }

    /* ========================= ENV WALLPAPER ========================== */

    public static String WALLPAPER_URL() {
        return Holder.WALLPAPER_URL;
    }
}

