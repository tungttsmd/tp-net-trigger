package tungtt.Security.Contexts;

public record HwidProfileContext(
    String hwCpuId,
    String hwDiskSerial,
    String hwRdp) {}