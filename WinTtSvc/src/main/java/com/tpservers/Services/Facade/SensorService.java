package com.tpservers.Services.Facade;

import com.google.gson.JsonObject;
import com.tpservers.Models.SensorState;
import tungtt.Console.Console;

public final class SensorService {
    private SensorService() {

    }

    static class Holder {
        static final SensorService INSTANCE = new SensorService();
        static int sensorFailCount = 0;
    }

    public static SensorService getInstance() {

        return Holder.INSTANCE;
    }

    /* ====================== RAW FROM WEB SERVER ============================ */

    public static JsonObject rawFromWebserver() {

        return SensorState.rawFromWebserver();
    }

    public static JsonObject temperatureFromWebserver() {

        return SensorState.temperatureFromWebserver();
    }

    public static JsonObject shortedSensorData() {

        JsonObject result = SensorState.shortedSensorData();
        if (result == null || result.size() == 0) {
            Holder.sensorFailCount++;
            Console.error("SensorService — dotnet webserver unreachable (" + Holder.sensorFailCount + "/" + ConfigService.SENSOR_MAX_FAIL_TO_REBOOT() + ")");
            if (Holder.sensorFailCount >= ConfigService.SENSOR_MAX_FAIL_TO_REBOOT()) {
                Console.error("SensorService — dotnet webserver dead. Exiting for reboot...");
                System.exit(95);
            }
        } else {
            Holder.sensorFailCount = 0;
        }
        return result;
    }
    /* ====================== SENSORS ============================ */

    public static JsonObject gpuSensors() {

        /* ====================== GPU SENSORS ============================ */

        return SensorState.gpuSensors();
    }

    public static JsonObject cpuSensors() {

        /* ====================== CPU SENSORS ============================ */

        return SensorState.cpuRead();
    }

    /* ====================== USAGE ============================ */

    public static JsonObject processUsage() {

        /* ====================== PROCESS USAGE ============================ */

        return SensorState.processUsage();
    }

    public static double getDiskUsagePercent(String path) {

        /* ====================== DISK USAGE ============================ */

        return SensorState.getDiskUsagePercent(path);
    }

    public static double getCpuUsagePercent() {

        /* ====================== CPU USAGE ============================ */

        return SensorState.getCpuUsagePercent();
    }

    public static double getRamUsagePercent() {

        /* ====================== RAM USAGE ============================ */

        return SensorState.getRamUsagePercent();
    }

}
