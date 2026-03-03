package com.tpservers.Services.Facade;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.Gson;

import tungtt.HardwareProfile.Modules.WindowsHardwareProfile;
import tungtt.Security.Contexts.HwidProfileContext;
import tungtt.Security.Modules.HwidGenerator;
import tungtt.HardwareProfile.Contexts.HardwareSnapshot;
import tungtt.HardwareProfile.Contexts.OsContext;
import oshi.SystemInfo;

public final class HardwareService {

    private HardwareService() {}

    static class Holder {

        static final WindowsHardwareProfile HW_PROFILE =
            new WindowsHardwareProfile(new OsContext(new SystemInfo()));

        static final HwidProfileContext HW_PROFILE_CONTEXT =
            new HwidProfileContext(
                Holder.HW_PROFILE.hwCpuId(),
                Holder.HW_PROFILE.hwDiskSerial(),
                Holder.HW_PROFILE.hwRdp(),
                Holder.HW_PROFILE.macAddresseList());

        static final HwidGenerator HW_HWID_GEN =
            new HwidGenerator(HW_PROFILE_CONTEXT);

        static final HardwareService INSTANCE = new HardwareService();
    }

    public static HardwareService getInstance() {

        return Holder.INSTANCE;
    }

    /* ====================== PROFILE SECTION ============================ */

    public static JsonObject getAll() {

        Gson gson = new Gson();
        HardwareSnapshot hw = Holder.HW_PROFILE.snapshot();

        JsonObject data = new JsonObject();
        data.addProperty("rdp", hw.rdp());
        data.addProperty("ip", hw.ip());
        data.add("mac", gson.toJsonTree(hw.macs()));
        data.add("dns", gson.toJsonTree(hw.dns()));
        data.add("cpu", gson.toJsonTree(hw.cpus()));
        data.add("ram", gson.toJsonTree(hw.rams()));
        data.add("gpu", gson.toJsonTree(hw.gpus()));
        data.add("disk", gson.toJsonTree(hw.disks()));
        data.add("motherboard", gson.toJsonTree(hw.motherboards()));
        data.add("os", gson.toJsonTree(hw.osInfo()));

        return data;
    }

    public static JsonObject getCpus() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("cpus", gson.toJsonTree(Holder.HW_PROFILE.cpuList()));
        return root;
    }

    public static JsonObject getGpus() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("gpus", gson.toJsonTree(Holder.HW_PROFILE.gpuList()));
        return root;
    }

    public static JsonObject getRams() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("rams", gson.toJsonTree(Holder.HW_PROFILE.ramList()));
        return root;
    }

    public static JsonObject getDisks() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("disks", gson.toJsonTree(Holder.HW_PROFILE.diskList()));
        return root;
    }

    public static JsonObject getMotherboard() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("motherboards", gson.toJsonTree(Holder.HW_PROFILE.motherboardList()));
        return root;
    }

    public static JsonObject getOs() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("os", gson.toJsonTree(Holder.HW_PROFILE.osInfo()));
        return root;
    }

    public static JsonObject getNetworks() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.addProperty("local_ip", Holder.HW_PROFILE.localIp());
        root.addProperty("port_number", Holder.HW_PROFILE.rdp());
        root.add("dns", gson.toJsonTree(Holder.HW_PROFILE.dnsServersList()));
        root.add("macs", gson.toJsonTree(Holder.HW_PROFILE.macAddresseList()));
        return root;
    }

    public static JsonObject getDevices() {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("cpus", gson.toJsonTree(Holder.HW_PROFILE.cpuList()));
        root.add("rams", gson.toJsonTree(Holder.HW_PROFILE.ramList()));
        root.add("disks", gson.toJsonTree(Holder.HW_PROFILE.diskList()));
        root.add("gpus", gson.toJsonTree(Holder.HW_PROFILE.gpuList()));
        root.add("motherboards", gson.toJsonTree(Holder.HW_PROFILE.motherboardList()));
        return root;
    }

    /* ====================== HWID SECTION ============================ */

    public static int getRdpPort() {

        return Holder.HW_PROFILE.rdp();
    }

    public static String getCpuId() {

        return Holder.HW_PROFILE.hwCpuId();
    }

    public static String getDiskSerial() {

        return Holder.HW_PROFILE.hwDiskSerial();
    }

    public static JsonObject collect() {

        HardwareSnapshot hw = Holder.HW_PROFILE.snapshot();
        JsonObject root = new JsonObject();

        Gson gson = new Gson();

        root.addProperty("rdp", hw.rdp());
        root.addProperty("ip", hw.ip());
        root.add("dns", gson.toJsonTree(hw.dns()));
        root.add("macs", gson.toJsonTree(hw.macs()));
        root.add("cpus", gson.toJsonTree(hw.cpus()));
        root.add("rams", gson.toJsonTree(hw.rams()));
        root.add("gpus", gson.toJsonTree(hw.gpus()));
        root.add("motherboards", gson.toJsonTree(hw.motherboards()));
        root.add("disks", gson.toJsonTree(hw.disks()));
        root.add("osInfo", gson.toJsonTree(hw.osInfo()));

        return root;
    }

    /* ===================== GETTER ===================== */

    public static String hwRdp() {

        return Holder.HW_PROFILE.hwRdp();
    }

    public static String hwDiskSerial() {

        return Holder.HW_PROFILE.hwDiskSerial();
    }

    public static String hwCpuId() {

        return Holder.HW_PROFILE.hwCpuId();
    }

    public static String hwHwid() {

        return Holder.HW_HWID_GEN.build();
    }

    public static List<String> getMacAddresses() {

        return Holder.HW_PROFILE.macAddresseList();
    }

    public static HwidProfileContext getHwidProfileContext() {

        return Holder.HW_PROFILE_CONTEXT;
    }

    public static String getLocalIp() {

        return Holder.HW_PROFILE.localIp();
    }
}
