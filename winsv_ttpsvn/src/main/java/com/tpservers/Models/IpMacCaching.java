package com.tpservers.Models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IpMacCaching {

    private static final String FILE_NAME = "mac_map.list.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // in-memory cache
    private static Map<String, String> CACHE;

    private IpMacCaching() {}

    /* ===================== PUBLIC API ===================== */

    /** Resolve IP -> MAC, chỉ ARP khi cần */
    public static String resolveMac(String ip) {
        if (ip == null || ip.isBlank()) return null;

        ensureLoaded();

        // 1. memory / file cache
        String mac = CACHE.get(ip);
        if (mac != null) return mac;

        // 2. fallback: ARP
        refreshFromArp();

        // 3. check lại
        return CACHE.get(ip);
    }

    /* ===================== INTERNAL ===================== */

    /** Load cache từ file một lần */
    private static synchronized void ensureLoaded() {
        if (CACHE != null) return;

        File f = file();
        if (!f.exists()) {
            CACHE = new HashMap<>();
            return;
        }

        try {
            CACHE = MAPPER.readValue(
                f,
                new TypeReference<Map<String, String>>() {}
            );
        } catch (Exception e) {
            CACHE = new HashMap<>();
        }
    }

    /** Chỉ gọi khi cache miss */
    private static synchronized void refreshFromArp() {
        Map<String, String> discovered = new HashMap<>();

        try {
            Process p = new ProcessBuilder("arp", "-a").start();

            Pattern pat = Pattern.compile(
                "([0-9.]+)\\s+([0-9a-fA-F-]{17})"
            );

            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = pat.matcher(line);
                    if (m.find()) {
                        discovered.put(
                            m.group(1),
                            m.group(2)
                                .replace("-", ":")
                                .toUpperCase()
                        );
                    }
                }
            }

            if (discovered.isEmpty()) return;

            // merge + persist
            CACHE.putAll(discovered);
            MAPPER.writerWithDefaultPrettyPrinter()
                .writeValue(file(), CACHE);

        } catch (Exception ignored) {
            // ARP fail thì coi như không có
        }
    }

    /** File cache nằm ở working directory (cạnh .env) */
    private static File file() {
        return new File(
            System.getProperty("user.dir"),
            FILE_NAME
        );
    }
}
