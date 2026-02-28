package tungtt.HardwareProfile.Contexts;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import oshi.software.os.NetworkParams;

public final class OsContext {
    
    private final HardwareAbstractionLayer hal;
    private final OperatingSystem os;

    public OsContext(SystemInfo systemInfo) {

        if (systemInfo == null) {
            throw new IllegalArgumentException("SystemInfo is null");
        }

        this.hal = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
    }


    public HardwareAbstractionLayer hal() {

        return this.hal;
    }

    public OperatingSystem os() {

        return this.os;
    }
}
