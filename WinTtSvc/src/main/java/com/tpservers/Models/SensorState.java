package com.tpservers.Models;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.tpservers.Core.RequestCore;
import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.ConsoleService;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.gson.JsonElement;

import oshi.hardware.NetworkIF;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;

public final class SensorState {

    private SensorState() {
    }

    final static class Holder {

        static final SystemInfo SI = new SystemInfo();
        static final HardwareAbstractionLayer HAL = SI.getHardware();
        static final CentralProcessor CPU = HAL.getProcessor();
        static final GlobalMemory MEM = HAL.getMemory();
        static final Sensors SENSORS = HAL.getSensors();
        static final boolean HAS_NVIDIA = detectNvidia();

        static long[] PREV_CPU_TICKS;
        static long PREV_NET_BYTES;
        static long PREV_NET_TIME;

        static final String REQUEST_CONTENT_TYPE = "application/json";
        static final String SENSOR_WEBSERVER_RAW_PATH = ConfigService.SENSOR_WEBSERVER_RAW_PATH();
        static final String SENSOR_WEBSERVER_HOST = ConfigService.SENSOR_WEBSERVER_HOST();

        static {
            PREV_NET_BYTES = 0;
            PREV_CPU_TICKS = CPU.getSystemCpuLoadTicks();
            PREV_NET_TIME = System.currentTimeMillis();
        }

        static final SensorState INSTANCE = new SensorState();
    }

    /* ====================== DATA FROM WEB SERVER ============================= */

    public static JsonObject rawFromWebserver() {

        try {

            JsonObject headers = new JsonObject();
            JsonObject payload = new JsonObject();

            String response = RequestCore.post(
                    Holder.SENSOR_WEBSERVER_HOST + Holder.SENSOR_WEBSERVER_RAW_PATH,
                    headers,
                    payload);

            if (response == null || response.isBlank()) {

                ConsoleService.error("Empty response");
                throw new RuntimeException("Empty response");
            }
            ConsoleService.info("Get sensor data from sensorWebserver successful");
            return sensorValidator(response);

        } catch (Exception e) {

            e.printStackTrace();
            ConsoleService.error("Connecting to sensorWebserver failed: " + e.getMessage());
            return new JsonObject();
        }
    }

    /* ====================== DATA FILTER ============================= */

    public static JsonObject shortedSensorData() {

        JsonObject result = new JsonObject();

        try {

            JsonObject raw = SensorState.rawFromWebserver();

            for (Map.Entry<String, JsonElement> deviceEntry : raw.entrySet()) {

                JsonObject device = deviceEntry.getValue().getAsJsonObject();
                if (!device.has("sensors")) continue;

                for (JsonElement sensorEl : device.getAsJsonArray("sensors")) {

                    JsonObject sensor = sensorEl.getAsJsonObject();

                    if (!sensor.has("type") || !sensor.has("name")) continue;
                    if (!sensor.has("value") || sensor.get("value").isJsonNull()) continue;

                    String type = sensor.get("type").getAsString();
                    String name = sensor.get("name").getAsString();
                    double value = round(sensor.get("value").getAsDouble());

                    /* CPU */
                    if ("Load".equals(type) &&
                            ("CPU Total".equals(name) || "CPU Core Max".equals(name))) {
                        result.addProperty("cpu_load", value);
                    }

                    if ("Temperature".equals(type) &&
                            ("CPU Package".equals(name) || "Core Max".equals(name))) {
                        result.addProperty("cpu_temp", value);
                    }

                    /* RAM */
                    if ("Load".equals(type) && "Memory".equals(name)) {
                        result.addProperty("ram_load", value);
                    }

                    /* GPU */
                    if ("Load".equals(type) && "GPU Core".equals(name)) {
                        result.addProperty("gpu_load", value);
                    }

                    if ("Temperature".equals(type) && "GPU Core".equals(name)) {
                        result.addProperty("gpu_temp", value);
                    }

                    /* FAN */
                    if ("Control".equals(type) && name.toLowerCase().contains("fan")) {
                        result.addProperty("fan_load", value);
                    }
                }
            }

            return result;

        } catch (Exception e) {

            ConsoleService.error("Sensor model got failed: " + e.getMessage());
            return new JsonObject();
        }
    }

    
    public static JsonObject temperatureFromWebserver() {

        JsonObject result = new JsonObject();
        JsonArray temps = new JsonArray();

        try {

            JsonObject raw = SensorState.rawFromWebserver();

            for (Map.Entry<String, JsonElement> deviceEntry : raw.entrySet()) {

                JsonObject device = deviceEntry.getValue().getAsJsonObject();
                if (!device.has("sensors")) continue;

                String deviceName = device.has("name")
                        ? device.get("name").getAsString()
                        : deviceEntry.getKey();

                for (JsonElement sensorEl : device.getAsJsonArray("sensors")) {

                    JsonObject sensor = sensorEl.getAsJsonObject();

                    if (!sensor.has("type") || !sensor.has("name")) continue;
                    if (!"Temperature".equals(sensor.get("type").getAsString())) continue;
                    if (!sensor.has("value") || sensor.get("value").isJsonNull()) continue;

                    JsonObject row = new JsonObject();
                    row.addProperty("device", deviceName);
                    row.addProperty("sensor", sensor.get("name").getAsString());
                    row.addProperty("value", round(sensor.get("value").getAsDouble()));

                    temps.add(row);
                }
            }

            result.add("temperatures", temps);
            return result;

        } catch (Exception e) {

            ConsoleService.error("Sensor model got failed: " + e.getMessage());
            result.add("temperatures", new JsonArray());
            result.addProperty("error", e.getMessage());
            return result;
        }
    }

    
    /* ====================== SENSOR VALIDATOR ============================ */

    public static JsonObject sensorValidator(String response) {

        try {
            JsonElement el = JsonParser.parseString(response);

            if (el.isJsonObject()) {
                return el.getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleService.error("Sensor data from sensorWebserver failed: " + e.getMessage());
            return new JsonObject();
        }

        throw new IllegalStateException("Unexpected JSON format");
    }

    /* ====================== RAM + CPU usage % ============================ */

    public static JsonObject processUsage() {

        try {
            JsonObject root = new JsonObject();

            // ===== META =====
            JsonObject meta = new JsonObject();
            meta.addProperty("agent_id", ConfigService.HOST_ID());

            // ===== SECURE =====
            JsonObject secure = new JsonObject();
            secure.addProperty("signature", "");

            // ===== DATA =====
            JsonObject data = new JsonObject();
            data.addProperty("cpu_usage_percent", getCpuUsagePercent());
            data.addProperty("ram_usage_percent", getRamUsagePercent());

            // ===== MERGE =====
            root.add("secure", secure);
            root.add("meta", meta);
            root.add("data", data);

            return root;
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleService.error("Sensor data from sensorWebserver failed: " + e.getMessage());
            return new JsonObject();
        }
    }

    /* ================= COMBO GPU POWER DRAW & TEMPERATURE ================= */

    public static JsonObject gpuSensors() {
        try {
            JsonObject root = new JsonObject();

            // ===== META =====
            JsonObject meta = new JsonObject();
            meta.addProperty("agent_id", ConfigService.HOST_ID());

            // ===== SECURE =====
            JsonObject secure = new JsonObject();
            secure.addProperty("signature", "");

            // ===== DATA =====
            JsonObject data = new JsonObject();
            data.addProperty("gpu_temperature_percent", getGpuTemp());
            data.addProperty("gpu_power_draw_watt", getGpuPowerDraw());

            // ===== MERGE =====
            root.add("secure", secure);
            root.add("meta", meta);
            root.add("data", data);

            return root;
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleService.error("Sensor data from sensorWebserver failed: " + e.getMessage());
            return new JsonObject();
        }
    }
    /* ================= CPU ================= */

    public static double getCpuUsagePercent() {
        try {
            // Tạo process để chạy lệnh PowerShell
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-Command",
                    "[math]::Round((Get-CimInstance Win32_Processor | " +
                            "Measure-Object -Property LoadPercentage -Average).Average, 2)");

            // Chuyển hướng lỗi để tránh bị đầy bộ đệm
            processBuilder.redirectErrorStream(true);
            Process p = processBuilder.start();

            // Đọc kết quả
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();

                // Đợi process kết thúc
                int exitCode = p.waitFor();

                if (exitCode != 0 || line == null || line.trim().isEmpty()) {
                    return -1;
                }

                return Double.parseDouble(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để debug
            return -1;
        }
    }

    /* ================= RAM ================= */

    public static double getRamUsagePercent() {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-Command",
                    "[math]::Round(((Get-CimInstance Win32_OperatingSystem).TotalVisibleMemorySize - " +
                            "(Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory) * 100 / " +
                            "(Get-CimInstance Win32_OperatingSystem).TotalVisibleMemorySize, 2)");

            // Chuyển hướng lỗi để tránh bị đầy bộ đệm
            processBuilder.redirectErrorStream(true);
            Process p = processBuilder.start();

            // Đọc kết quả
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();

                // Đợi process kết thúc
                int exitCode = p.waitFor();

                if (exitCode != 0 || line == null || line.trim().isEmpty()) {
                    return -1;
                }

                return Double.parseDouble(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để debug
            return -1;
        }
    }

    /* ================= DISK ================= */

    public static double getDiskUsagePercent(String path) {

        try {
            File disk = new File(path);

            if (!disk.exists())
                return -1;

            long total = disk.getTotalSpace();
            long free = disk.getUsableSpace();

            if (total <= 0)
                return -1;

            double used = ((double) (total - free) / total) * 100.0;
            return round(used);
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để debug
            return -1;
        }
    }

    /* ================= GPU SENSOR =========== */

    private static boolean detectNvidia() {
        try {
            SystemInfo si = new SystemInfo();
            return si.getHardware()
                    .getGraphicsCards()
                    .stream()
                    .anyMatch(g -> g.getVendor() != null &&
                            g.getVendor().toLowerCase().contains("nvidia"));
        } catch (Exception e) {
            return false;
        }
    }

    private static Double gpuRun(String query) {
        if (!Holder.HAS_NVIDIA)
            return null;

        try {
            Process p = new ProcessBuilder(
                    "nvidia-smi",
                    "--query-gpu=" + query,
                    "--format=csv,noheader,nounits").start();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {

                String line = br.readLine();
                p.waitFor();

                if (line == null || line.isBlank())
                    return null;
                return Double.parseDouble(line.trim());
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Double getGpuTemp() {

        return gpuRun("temperature.gpu");
    }

    public static Double getGpuPowerDraw() {

        return gpuRun("power.draw");
    }

    /* ================= CPU SENSOR =========== */

    public static JsonObject cpuRead() {

        try {
            /* ================= CPU ================= */
            double cpuLoad = Holder.CPU.getSystemCpuLoadBetweenTicks(Holder.PREV_CPU_TICKS) * 100.0;
            Holder.PREV_CPU_TICKS = Holder.CPU.getSystemCpuLoadTicks();

            /* ================= SENSORS ================= */
            Sensors sensors = Holder.SENSORS;
            double cpuTemp = sensors.getCpuTemperature();
            int[] fanSpeeds = sensors.getFanSpeeds();
            Double gpuTemp = getGpuTemp();
            Double gpuPowerDraw = getGpuPowerDraw();

            /* ================= RAM ================= */
            double ramUsage = 100.0 * (Holder.MEM.getTotal() - Holder.MEM.getAvailable()) / Holder.MEM.getTotal();

            /* ================= DISK ================= */
            double diskUsage = getDiskUsagePercent();

            /* ================= NETWORK ================= */
            double netUsage = getNetworkMbps();

            /* ================= JSON BUILD ================= */
            JsonObject root = new JsonObject();
            JsonObject data = new JsonObject();
            System.out.println("GPU Temp: " + gpuTemp);
            System.out.println("GPU Power Draw: " + gpuPowerDraw);
            System.out.println("CPU Load: " + cpuLoad);
            System.out.println("CPU Temp: " + cpuTemp);
            System.out.println("Fan Speed: " + Arrays.toString(fanSpeeds));
            System.out.println("RAM Usage: " + ramUsage);
            System.out.println("Disk Usage: " + diskUsage);
            System.out.println("Network Usage: " + netUsage);

            /* ---- temp ---- */
            JsonObject temp = new JsonObject();
            temp.addProperty("cpu", round(cpuTemp));
            temp.addProperty("gpu", gpuTemp);
            temp.addProperty("vrm", (String) null);
            temp.addProperty("storage", (String) null);
            temp.addProperty("ram", (String) null);
            temp.addProperty("motherboard", (String) null);

            /* ---- power ---- */
            JsonObject power = new JsonObject();
            power.addProperty("cpu_package_power", (String) null);
            power.addProperty("gpu_power_draw", gpuPowerDraw);

            /* ---- operate ---- */
            JsonObject operate = new JsonObject();
            operate.addProperty(
                    "fan_speed",
                    fanSpeeds.length > 0 ? Arrays.stream(fanSpeeds).average().orElse(0) : null);
            operate.addProperty("fan_duty_cycle", (String) null);

            /* ---- usage ---- */
            JsonObject usage = new JsonObject();
            usage.addProperty("cpu", round(cpuLoad));
            usage.addProperty("ram", round(ramUsage));
            usage.addProperty("gpu", "-1");
            usage.addProperty("storage", round(diskUsage));
            usage.addProperty("network", round(netUsage));

            data.add("temp", temp);
            data.add("power", power);
            data.add("operate", operate);
            data.add("usage", usage);

            root.add("data", data);
            return root;
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để debug
            return new JsonObject();
        }
    }

    /* ================= HELPERS ================= */

    private static double getDiskUsagePercent() {
        List<OSFileStore> stores = Holder.SI.getOperatingSystem().getFileSystem().getFileStores();
        long total = 0, used = 0;
        for (OSFileStore fs : stores) {
            total += fs.getTotalSpace();
            used += (fs.getTotalSpace() - fs.getUsableSpace());
        }
        if (total == 0)
            return 0;
        return 100.0 * used / total;
    }

    private static double getNetworkMbps() {
        long now = System.currentTimeMillis();
        long bytes = 0;

        for (NetworkIF net : Holder.HAL.getNetworkIFs()) {
            net.updateAttributes();
            bytes += net.getBytesRecv() + net.getBytesSent();
        }

        long deltaBytes = bytes - Holder.PREV_NET_BYTES;
        long deltaTime = now - Holder.PREV_NET_TIME;

        Holder.PREV_NET_BYTES = bytes;
        Holder.PREV_NET_TIME = now;

        if (deltaTime <= 0)
            return 0;
        return (deltaBytes * 8.0) / deltaTime / 1000; // Mbps
    }

    private static double round(double v) {
        return BigDecimal.valueOf(v)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
