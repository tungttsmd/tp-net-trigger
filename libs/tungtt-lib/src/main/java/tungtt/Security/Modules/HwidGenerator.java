package tungtt.Security.Modules;

import tungtt.Security.Contexts.HwidProfileContext;
import tungtt.Security.Abstracts.AbstractHwidSource;

public final class HwidGenerator extends AbstractHwidSource {

    public HwidGenerator(HwidProfileContext hwProfileContext) {

        super(hwProfileContext);
    }

    @Override
    protected String hwidEncode() {

        return tail(this.hwProfileContext.hwCpuId(), 8)
            + "-"
            + tail(this.hwProfileContext.hwDiskSerial(), 8);
    }

    private String tail(String input, int cutLen) {

        if (input == null || input.isBlank()) {
            return fallback(cutLen);
        }

        String cleaned = input.replaceAll("[^A-Za-z0-9]", "");

        if (cleaned.length() < cutLen) {
            cleaned = fallback(cutLen);
        }

        return cleaned.length() <= cutLen
            ? cleaned
            : cleaned.substring(cleaned.length() - cutLen);
    }

    private String fallback(int len) {
        String v = "k" + Math.abs(System.nanoTime());
        return v.length() <= len
            ? v
            : v.substring(v.length() - len);
    }
}
