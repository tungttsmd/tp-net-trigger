package tungtt.HardwareProfile.Modules;

import java.net.Socket;
import java.net.InetSocketAddress;

import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import oshi.hardware.*;
import oshi.software.os.NetworkParams;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import tungtt.HardwareProfile.Contexts.*;
import tungtt.HardwareProfile.Exceptions.HardwareProfileException;
import tungtt.HardwareProfile.Interfaces.HardwareProfileProvider;

public final class WindowsHardwareProfile
        implements HardwareProfileProvider {

    private final OsContext osContext;

    public WindowsHardwareProfile(OsContext osContext) {

        if (osContext == null) {
            throw new IllegalArgumentException("OsContext is null");
        }

        this.osContext = osContext;
    }

    @Override
    public HardwareSnapshot snapshot() {

        return new HardwareSnapshot(
                this.rdp(),
                this.localIp(),
                this.macAddresseList(),
                this.dnsServersList(),
                this.cpuList(),
                this.ramList(),
                this.gpuList(),
                this.diskList(),
                this.motherboardList(),
                this.osInfo(),
                this.hwCpuId(),
                this.hwDiskSerial(),
                String.valueOf(this.hwRdp())
        );
    }

    /* ============================================================== */
    /* =========================== GETTER =========================== */
    /* ============================================================== */
    
    /* HWID CPU ID PREPARE */
    public String hwCpuId() {

        try {
            CentralProcessor cpu = this.osContext.hal().getProcessor();
            String id = cpu.getProcessorIdentifier().getProcessorID();
            return (id == null || id.isBlank()) ? "unknown" : id.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    /* HWID DISK SERIAL PREPARE */
    public String hwDiskSerial() {

        try {
            if (this.osContext.hal().getDiskStores().isEmpty())
                return "unknown";

            for (HWDiskStore d : this.osContext.hal().getDiskStores()) {
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

    /* HWID RDP PORT PREPARE */
    public String hwRdp() {

        try {
            return String.valueOf(Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "SYSTEM\\CurrentControlSet\\Control\\Terminal Server\\WinStations\\RDP-Tcp",
                    "PortNumber"));
        } catch (Exception e) {
            e.printStackTrace();
            return "3389";
        }
    }

    /* RDP PORT */
    public int rdp() {

        try {

            return Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "SYSTEM\\CurrentControlSet\\Control\\Terminal Server\\WinStations\\RDP-Tcp",
                    "PortNumber");
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read rdp port", e);
        }
    }

    /* LOCAL IP */
    public String localIp() {

        try {
            try (Socket socket = new Socket()) {

                socket.connect(new InetSocketAddress("8.8.8.8", 53));
                return socket.getLocalAddress().getHostAddress();
            } catch (Exception e) {
                throw new HardwareProfileException("Failed to read local ip", e);
            }
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read local ip", e);
        }
    }

    /* MAC INFO */
    public List<String> macAddresseList() {

        List<String> macs = new ArrayList<>();
        try {

            for (NetworkIF net : this.osContext.hal().getNetworkIFs()) {

                net.updateAttributes();
                if (net.getIPv4addr().length == 0)
                    continue;

                String mac = net.getMacaddr();
                if (mac == null || mac.isEmpty() || mac.equalsIgnoreCase("00:00:00:00:00:00"))
                    continue;
                macs.add(mac.toLowerCase());
            }
            return macs;
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read mac info", e);
        }
    }

    /* DNS INFO */
    public List<String> dnsServersList() {

        try {
            NetworkParams params = this.osContext.os().getNetworkParams();
            return Arrays.asList(params.getDnsServers());
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read dns info", e);
        }
    }

    /* CPU INFO */
    public List<Map<String, Object>> cpuList() {

        List<Map<String, Object>> list = new ArrayList<>();

        try {

            CentralProcessor p = this.osContext.hal().getProcessor();

            Map<String, Object> cpu = new LinkedHashMap<>();
            cpu.put("socket", 0);
            cpu.put("name", p.getProcessorIdentifier().getName());
            cpu.put("cores", p.getPhysicalProcessorCount());
            cpu.put("threads", p.getLogicalProcessorCount());
            cpu.put("max_clock_mhz", p.getMaxFreq() / 1_000_000);
            cpu.put("id", p.getProcessorIdentifier().getProcessorID());

            list.add(cpu);
            return list;
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read cpu info", e);
        }
    }

    /* RAM INFO */
    public List<Map<String, Object>> ramList() {

        List<Map<String, Object>> list = new ArrayList<>();
        int index = 0;
        try {

            for (PhysicalMemory m : this.osContext.hal().getMemory().getPhysicalMemory()) {
                Map<String, Object> ram = new LinkedHashMap<>();

                ram.put("slot", index++);
                ram.put("manufacturer", m.getManufacturer());
                ram.put("capacity_gb", m.getCapacity() / (1024.0 * 1024 * 1024));
                ram.put("speed_mhz", m.getClockSpeed() / 1_000_000);

                list.add(ram);
            }
            return list;
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read ram info", e);
        }
    }

    /* GPU INFO */
    public List<Map<String, Object>> gpuList() {

        List<Map<String, Object>> list = new ArrayList<>();
        try {

            String cmd = """
                        Get-CimInstance Win32_VideoController |
                        Where-Object {
                            $_.Status -eq 'OK' -and
                            $_.CurrentHorizontalResolution -gt 0 -and
                            $_.CurrentVerticalResolution -gt 0
                        } |
                        Select Name, DriverVersion, AdapterRAM |
                        ConvertTo-Json
                    """;

            String json = this.runPowerShell(cmd, 10);
            if (json.isEmpty())
                return list;

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
            return list;
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read gpu info", e);
        }
    }

    /* DISK INFO */
    public List<Map<String, Object>> diskList() {

        List<Map<String, Object>> list = new ArrayList<>();
        int index = 0;
        try {

            for (HWDiskStore d : this.osContext.hal().getDiskStores()) {

                Map<String, Object> disk = new LinkedHashMap<>();
                disk.put("index", index++);
                disk.put("model", d.getModel());
                disk.put("serial", d.getSerial());
                disk.put("size_gb", d.getSize() / (1024.0 * 1024 * 1024));
                list.add(disk);
            }
            return list;
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read disk info", e);
        }
    }

    /* MOTHERBOARD INFO */
    public List<Map<String, Object>> motherboardList() {

        List<Map<String, Object>> list = new ArrayList<>();
        try {

            Baseboard b = this.osContext.hal().getComputerSystem().getBaseboard();

            Map<String, Object> board = new LinkedHashMap<>();
            board.put("manufacturer", b.getManufacturer());
            board.put("product", b.getModel());
            board.put("serial", b.getSerialNumber());
            board.put("version", b.getVersion());
            list.add(board);
            return list;
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read motherboard info", e);
        }
    }

    /* OS INFO */
    public Map<String, Object> osInfo() {

        Map<String, Object> map = new LinkedHashMap<>();
        try {

            String cmd = """
                        Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion' |
                        Select ProductName, DisplayVersion, ReleaseId, CurrentBuild, UBR, InstallDate |
                        ConvertTo-Json
                    """;

            String json = this.runPowerShell(cmd, 10);
            if (json.isEmpty())
                return map;

            JsonObject o = JsonParser.parseString(json).getAsJsonObject();

            map.put("caption", o.get("ProductName").getAsString());
            map.put("version",
                    o.get("DisplayVersion") != null && !o.get("DisplayVersion").isJsonNull()
                            ? o.get("DisplayVersion").getAsString()
                            : o.get("ReleaseId").getAsString());
            map.put("build", o.get("CurrentBuild").getAsString());
            if (o.has("InstallDate") && !o.get("InstallDate").isJsonNull()) {

                long installTs = o.get("InstallDate").getAsLong();
                LocalDateTime installDate = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(installTs),
                        ZoneId.systemDefault());

                map.put("installed_at", installDate.toString()); // ISO-8601
            }
            return map;
        } catch (Exception e) {
            throw new HardwareProfileException("Failed to read os info", e);
        }
    }

    /* HELPER */
    private String runPowerShell(String cmd, int timeoutSec) {

        try {
            Process p = new ProcessBuilder("powershell", "-Command", cmd).start();

            if (!p.waitFor(timeoutSec, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new RuntimeException("PowerShell timeout");
            }

            if (p.exitValue() != 0) {
                throw new RuntimeException(
                        new String(p.getErrorStream().readAllBytes()));
            }

            return new String(p.getInputStream().readAllBytes()).trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}