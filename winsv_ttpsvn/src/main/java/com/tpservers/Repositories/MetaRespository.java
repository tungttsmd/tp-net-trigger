package com.tpservers.Repositories;

import com.tpservers.Services.Facade.HardwareService;
import tungtt.Security.Modules.MacHwidGenerator;

public final class MetaRespository {

    private MetaRespository() {
    }

    private static class Holder {

        private static final int    HOST_ID       = HardwareService.getRdpPort();
        private static final String HOST_HWID     = MetaRespository.hwidEncode();
        private static final String HOST_LOCAL_IP = HardwareService.getLocalIp();

        private static final String HOST_FROM =
            Holder.HOST_LOCAL_IP + "-" +
            System.getProperty("HOST_FROM_PREFIX") + "-" +
            MetaRespository.hostId() + "-" +
            MetaRespository.hostHwid();

        private static final String HOST_VERSION = System.getProperty("HOST_VERSION");

        private static final MetaRespository INSTANCE = new MetaRespository();
    }

    /*
     * =============================================================================
     * ========================== CUSTOM ===========================================
     * =============================================================================
     */

    /*
     * =============================================================================
     * ========================== FIXED ============================================
     * =============================================================================
     */

    public static MetaRespository getInstance() {
        return Holder.INSTANCE;
    }

    public static String hostFrom() {
        return Holder.HOST_FROM;
    }

    public static String hostHwid() {
        return Holder.HOST_HWID;
    }

    public static String hostVersion() {
        return Holder.HOST_VERSION;
    }

    public static int hostId() {
        return Holder.HOST_ID;
    }

    public static String hostLocalIp() {
        return Holder.HOST_LOCAL_IP;
    }

    /*
     * =============================================================================
     * Mã hoá HWID
     * =============================================================================
     */
    private static String hwidEncode() {
        return new MacHwidGenerator(HardwareService.getHwidProfileContext()).build();
    }

}
