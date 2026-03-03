package tungtt.Security.Modules;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import tungtt.Security.Abstracts.AbstractHwidSource;
import tungtt.Security.Contexts.HwidProfileContext;

public final class MacHwidGenerator extends AbstractHwidSource {

    public MacHwidGenerator(HwidProfileContext hwProfileContext) {

        super(hwProfileContext);
    }

    @Override
    protected String hwidEncode() {

        List<String> macs = this.hwProfileContext.macs();

        if (macs == null || macs.isEmpty()) {
            return "00000000";
        }

        List<String> sorted = macs.stream()
                .map(String::toLowerCase)
                .sorted()
                .toList();

        String combined = String.join("", sorted);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString().substring(0, 8).toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có sẵn trong JVM chuẩn, không thể xảy ra
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
