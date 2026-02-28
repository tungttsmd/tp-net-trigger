package com.tpservers.Models;

import oshi.SystemInfo;
import oshi.hardware.*;

import oshi.software.os.OperatingSystem;
import oshi.software.os.NetworkParams;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public final class HardwareProfile {
    private HardwareProfile() {
    }

    private static class Holder {
        private static final SystemInfo systemInfo;
        private static final HardwareAbstractionLayer hal;
        private static final OperatingSystem os;

        static {
            systemInfo = new SystemInfo();
            hal = systemInfo.getHardware();
            os = systemInfo.getOperatingSystem();
        }

    }

    public static JsonObject getAll() {

        JsonObject data = new JsonObject();
        data.addProperty("rdp", rdp());
        data.addProperty("ip", localIp());
        data.add("mac", macAddresseList());
        data.add("dns", dnsServersList());
        data.add("cpu", cpuList());
        data.add("ram", ramList());
        data.add("gpu", gpuList());
        data.add("disk", diskList());
        data.add("motherboard", motherboardList());
        data.add("os", os());

        return data;
    }

    public static int rdp() {

        try {
            return Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "SYSTEM\\CurrentControlSet\\Control\\Terminal Server\\WinStations\\RDP-Tcp",
                    "PortNumber");
        } catch (Exception e) {
            e.printStackTrace();
            return 3389;
        }
    }

    public static String localIp() {

        try {
            try (Socket socket = new Socket()) {

                socket.connect(new InetSocketAddress("8.8.8.8", 53));
                return socket.getLocalAddress().getHostAddress();
            } catch (Exception e) {
                e.printStackTrace();
                return "unknown";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    public static JsonArray macAddresseList() {

        List<String> macs = new ArrayList<>();
        try {
            for (NetworkIF net : Holder.hal.getNetworkIFs()) {

                net.updateAttributes();
                if (net.getIPv4addr().length == 0)
                    continue;

                String mac = net.getMacaddr();
                if (mac == null || mac.isEmpty() || mac.equalsIgnoreCase("00:00:00:00:00:00"))
                    continue;
                macs.add(mac.toLowerCase());
            }
            return JsonParser.parseString(new Gson().toJson(macs)).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonParser.parseString(new Gson().toJson(new ArrayList<>())).getAsJsonArray();
        }
    }

    public static JsonArray dnsServersList() {

        try {
            NetworkParams params = Holder.os.getNetworkParams();
            return JsonParser.parseString(new Gson().toJson(params.getDnsServers())).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonParser.parseString(new Gson().toJson(new ArrayList<>())).getAsJsonArray();
        }
    }

    public static JsonArray cpuList() {
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            CentralProcessor p = Holder.hal.getProcessor();

            Map<String, Object> cpu = new LinkedHashMap<>();
            cpu.put("socket", 0);
            cpu.put("name", p.getProcessorIdentifier().getName());
            cpu.put("cores", p.getPhysicalProcessorCount());
            cpu.put("threads", p.getLogicalProcessorCount());
            cpu.put("max_clock_mhz", p.getMaxFreq() / 1_000_000);
            cpu.put("id", p.getProcessorIdentifier().getProcessorID());

            list.add(cpu);
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        }
    }

    public static JsonArray ramList() {

        List<Map<String, Object>> list = new ArrayList<>();
        int index = 0;
        try {
            for (PhysicalMemory m : Holder.hal.getMemory().getPhysicalMemory()) {
                Map<String, Object> ram = new LinkedHashMap<>();

                ram.put("slot", index++);
                ram.put("manufacturer", m.getManufacturer());
                ram.put("capacity_gb", m.getCapacity() / (1024.0 * 1024 * 1024));
                ram.put("speed_mhz", m.getClockSpeed() / 1_000_000);

                list.add(ram);
            }
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        }
    }

    public static JsonArray gpuList() {

        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Process p = new ProcessBuilder(
                    "powershell",
                    "-Command",
                    """
                            Get-CimInstance Win32_VideoController |
                            Where-Object {
                                $_.Status -eq 'OK' -and
                                $_.CurrentHorizontalResolution -gt 0 -and
                                $_.CurrentVerticalResolution -gt 0
                            } |
                            Select Name, DriverVersion, AdapterRAM |
                            ConvertTo-Json
                            """).start();

            String json = new String(p.getInputStream().readAllBytes()).trim();
            if (json.isEmpty())
                return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();

            JsonElement el = JsonParser.parseString(json);
            JsonArray arr;

            if (el.isJsonArray()) {
                arr = el.getAsJsonArray();
            } else {
                arr = new JsonArray();
                arr.add(el.getAsJsonObject());
            }

            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();

                JsonElement adapterRam = o.get("AdapterRAM");
                if (adapterRam == null || adapterRam.isJsonNull()) {
                    continue;
                }

                Map<String, Object> gpu = new LinkedHashMap<>();
                gpu.put("name", o.get("Name").getAsString());
                gpu.put("driver_version", o.get("DriverVersion").getAsString());

                list.add(gpu);
            }
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        }

    }

    public static JsonArray diskList() {

        List<Map<String, Object>> list = new ArrayList<>();
        int index = 0;
        try {

            for (HWDiskStore d : Holder.hal.getDiskStores()) {
                Map<String, Object> disk = new LinkedHashMap<>();
                disk.put("index", index++);
                disk.put("model", d.getModel());
                disk.put("serial", d.getSerial());
                disk.put("size_gb", d.getSize() / (1024.0 * 1024 * 1024));
                list.add(disk);
            }
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        }
    }

    public static JsonArray motherboardList() {

        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Baseboard b = Holder.hal.getComputerSystem().getBaseboard();

            Map<String, Object> board = new LinkedHashMap<>();
            board.put("manufacturer", b.getManufacturer());
            board.put("product", b.getModel());
            board.put("serial", b.getSerialNumber());
            board.put("version", b.getVersion());
            list.add(board);
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonParser.parseString(new Gson().toJson(list)).getAsJsonArray();
        }
    }

    public static JsonObject os() {

        JsonObject result = new JsonObject();

        try {
            Process p = new ProcessBuilder(
                    "powershell",
                    "-Command",
                    """
                    Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion' |
                    Select ProductName, DisplayVersion, ReleaseId, CurrentBuild, UBR, InstallDate |
                    ConvertTo-Json
                    """
            ).start();

            String json = new String(p.getInputStream().readAllBytes()).trim();
            if (json.isEmpty()) {
                return result;
            }

            JsonObject o = JsonParser.parseString(json).getAsJsonObject();

            result.addProperty("caption", o.get("ProductName").getAsString());

            if (o.has("DisplayVersion") && !o.get("DisplayVersion").isJsonNull()) {
                result.addProperty("version", o.get("DisplayVersion").getAsString());
            } else if (o.has("ReleaseId") && !o.get("ReleaseId").isJsonNull()) {
                result.addProperty("version", o.get("ReleaseId").getAsString());
            }

            result.addProperty("build", o.get("CurrentBuild").getAsString());

            if (o.has("InstallDate") && !o.get("InstallDate").isJsonNull()) {
                long installTs = o.get("InstallDate").getAsLong();
                LocalDateTime installDate = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(installTs),
                        ZoneId.systemDefault()
                );
                result.addProperty("installed_at", installDate.toString());
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }


    /* =========================== GETTER =========================== */
    public static String cpuId() {

        try {
            CentralProcessor cpu = Holder.hal.getProcessor();
            String id = cpu.getProcessorIdentifier().getProcessorID();
            return (id == null || id.isBlank()) ? "unknown" : id.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    public static String diskSerial() {

        try {
            if (Holder.hal.getDiskStores().isEmpty())
                return "unknown";

            for (HWDiskStore d : Holder.hal.getDiskStores()) {
                String serial = d.getSerial();
                if (serial != null && !serial.isBlank())
                    return serial.trim();
            }
            return "unknown";
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    public static int rdpPort() {

        try {
            return Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "SYSTEM\\CurrentControlSet\\Control\\Terminal Server\\WinStations\\RDP-Tcp",
                    "PortNumber");
        } catch (Exception e) {
            e.printStackTrace();
            return 3389;
        }
    }
}
