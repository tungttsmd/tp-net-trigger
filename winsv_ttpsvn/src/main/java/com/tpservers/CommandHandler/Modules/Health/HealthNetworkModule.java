package com.tpservers.CommandHandler.Modules.Health;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.EnvelopPublisher.ReportPublisher;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.CommandService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ModuleAnnotation
public final class HealthNetworkModule implements CommandModule {

    @Override
    public String title() {
        return "health";
    }

    @Override
    public String command() {
        return "health-network";
    }

    @Override
    public void handle(CommandContext ctx) {

        int pingCount = ConfigService.HEALTH_NETWORK_PING_COUNT();
        String targetsRaw = ConfigService.HEALTH_NETWORK_TARGETS();

        String commandContext = ctx.data().has("command_context")
                ? ctx.data().get("command_context").getAsString()
                : null;

        if (commandContext != null && !commandContext.isBlank()) {
            try {
                JsonObject args = JsonParser.parseString(commandContext).getAsJsonObject();
                if (args.has("ping_count")) pingCount = args.get("ping_count").getAsInt();
                if (args.has("targets"))    targetsRaw = args.get("targets").getAsString();
            } catch (Exception ignored) {}
        }

        final int finalPingCount = pingCount;
        List<String> targets = parseTargets(targetsRaw);

        ConcurrentHashMap<String, JsonObject> results = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(targets.size());

        for (String target : targets) {
            executor.submit(() -> results.put(target, ping(target, finalPingCount)));
        }

        executor.shutdown();

        long timeoutSeconds = (long) finalPingCount * 2;
        try {
            executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executor.shutdownNow();

        JsonObject payload = new JsonObject();
        for (String target : targets) {
            JsonObject r = results.getOrDefault(target, unreachable());
            payload.add(target, r);
        }

        ReportPublisher reportPublisher = new ReportPublisher(
                ConfigService.HEALTH_TOPIC(),
                CommandService.getInstance());

        reportPublisher.publish(title(), command(), ctx, payload);
    }

    private static List<String> parseTargets(String raw) {
        List<String> list = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            list.add("8.8.8.8");
            return list;
        }
        for (String t : raw.split(",")) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        if (list.isEmpty()) list.add("8.8.8.8");
        return list;
    }

    private static JsonObject ping(String target, int count) {
        try {
            Process process = new ProcessBuilder("ping", "-n", String.valueOf(count), target)
                    .redirectErrorStream(true)
                    .start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();

            return parsePingOutput(output.toString());

        } catch (Exception e) {
            return unreachable();
        }
    }

    private static JsonObject parsePingOutput(String output) {
        JsonObject result = new JsonObject();

        int avgMs = -1;
        int lossPct = 100;

        for (String line : output.split("\n")) {
            String trimmed = line.trim();

            if (trimmed.contains("Average =")) {
                int idx = trimmed.indexOf("Average =");
                String after = trimmed.substring(idx + 9).trim();
                String num = after.replaceAll("[^0-9]", "");
                if (!num.isEmpty()) {
                    try { avgMs = Integer.parseInt(num); } catch (NumberFormatException ignored) {}
                }
            }

            if (trimmed.contains("Lost =")) {
                int idx = trimmed.indexOf("Lost =");
                String after = trimmed.substring(idx + 6).trim();
                int parenOpen = after.indexOf('(');
                int parenClose = after.indexOf('%');
                if (parenOpen >= 0 && parenClose > parenOpen) {
                    String pct = after.substring(parenOpen + 1, parenClose).trim();
                    try { lossPct = Integer.parseInt(pct); } catch (NumberFormatException ignored) {}
                }
            }
        }

        boolean reachable = avgMs >= 0;
        result.addProperty("avg_ms", reachable ? avgMs : -1);
        result.addProperty("loss_pct", lossPct);
        result.addProperty("status", reachable ? "reachable" : "unreachable");

        return result;
    }

    private static JsonObject unreachable() {
        JsonObject r = new JsonObject();
        r.addProperty("avg_ms", -1);
        r.addProperty("loss_pct", 100);
        r.addProperty("status", "unreachable");
        return r;
    }
}
