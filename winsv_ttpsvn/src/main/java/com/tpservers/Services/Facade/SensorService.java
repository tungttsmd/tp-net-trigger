package com.tpservers.Services.Facade;

import com.google.gson.JsonObject;
import com.tpservers.Models.SensorState;

public final class SensorService {
    private SensorService() {

    }

    static class Holder {
        static final SensorService INSTANCE = new SensorService();
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

        return SensorState.shortedSensorData();
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
