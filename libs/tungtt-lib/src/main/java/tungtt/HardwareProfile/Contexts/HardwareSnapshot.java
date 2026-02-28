package tungtt.HardwareProfile.Contexts;

import java.util.List;
import java.util.Map;

public record HardwareSnapshot(
        int rdp,
        String ip,
        List<String> macs,
        List<String> dns,
        List<Map<String, Object>> cpus,
        List<Map<String, Object>> rams,
        List<Map<String, Object>> gpus,
        List<Map<String, Object>> disks,
        List<Map<String, Object>> motherboards,
        Map<String, Object> osInfo,
        String hwidCpuId,
        String hwidDiskSerial,
        String hwidRdpPort
) {}