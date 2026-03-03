package com.tpservers.Models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WakeOnLanMacSolver {

    private static final String FILE_NAME = "mac_map.list.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    static {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "arp-refresh");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(
            WakeOnLanMacSolver::refreshFromArp,
            0, 24, TimeUnit.HOURS
        );
    }

    private WakeOnLanMacSolver() {}

    public static Map<String, String> getCache() {
        return Collections.unmodifiableMap(CACHE);
    }

    public static String resolveMac(String ip) {
        if (ip == null || ip.isBlank()) return null;

        String mac = CACHE.get(ip);
        if (mac != null) return mac;

        refreshFromArp();

        return CACHE.get(ip);
    }

    private static synchronized void refreshFromArp() {
        Map<String, String> discovered = new HashMap<>();

        try {
            Process p = new ProcessBuilder("arp", "-a").start();

            Pattern pat = Pattern.compile(
                "([0-9.]+)\s+([0-9a-fA-F-]{17})"
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

            CACHE.putAll(discovered);
            MAPPER.writerWithDefaultPrettyPrinter()
                .writeValue(file(), CACHE);

        } catch (Exception ignored) {}
    }

    private static File file() {
        return new File(
            System.getProperty("user.dir"),
            FILE_NAME
        );
    }
}
