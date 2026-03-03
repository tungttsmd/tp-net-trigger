package com.tpservers.Services.Facade;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.Gson;


import com.tpservers.Models.HardwareProfile;

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

        return HardwareProfile.getAll();
    }

    public static JsonObject getCpus() {

        JsonObject root = new JsonObject();

        root.add("cpus", HardwareProfile.cpuList());

        return root;

    }

    public static JsonObject getGpus() {

        JsonObject root = new JsonObject();

        root.add("gpus", HardwareProfile.gpuList());

        return root;
    }

    public static JsonObject getRams() {

        JsonObject root = new JsonObject();

        root.add("rams", HardwareProfile.ramList());

        return root;

    }

    public static JsonObject getDisks() {

        JsonObject root = new JsonObject();

        root.add("disks", HardwareProfile.diskList());

        return root;

    }

    public static JsonObject getMotherboard() {

        JsonObject root = new JsonObject();

        root.add("motherboards", HardwareProfile.motherboardList());

        return root;

    }

    public static JsonObject getOs() {

        JsonObject root = new JsonObject();

        root.add("os", HardwareProfile.os());

        return root;

    }

    public static JsonObject getNetworks() {

        JsonObject root = new JsonObject();

        root.addProperty("local_ip", HardwareProfile.localIp());
        root.addProperty("port_number", HardwareProfile.rdp());
        root.add("dns", HardwareProfile.macAddresseList());
        root.add("macs", HardwareProfile.macAddresseList());

        return root;
    }

    public static JsonObject getDevices() {

        JsonObject root = new JsonObject();

        root.add("cpus", HardwareProfile.cpuList());
        root.add("rams", HardwareProfile.ramList());
        root.add("disks", HardwareProfile.diskList());
        root.add("gpus", HardwareProfile.gpuList());
        root.add("motherboards", HardwareProfile.motherboardList());

        return root;
    }

    /* ====================== HWID SECTION ============================ */

    public static int getRdpPort() {

        return HardwareProfile.rdpPort();
    }

    public static String getCpuId() {

        return HardwareProfile.cpuId();
    }

    public static String getDiskSerial() {

        return HardwareProfile.diskSerial();
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
}
