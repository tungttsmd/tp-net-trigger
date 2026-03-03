package tungtt.Security.Contexts;

import java.util.List;

public record HwidProfileContext(
    String hwCpuId,
    String hwDiskSerial,
    String hwRdp,
    List<String> macs) {}