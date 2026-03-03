package com.tpservers.Services.Facade;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import tungtt.Handler.CommandHandler.EnvelopPublisher.HeartbeatPublisher;
import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;

import com.tpservers.Repositories.MetaRespository;
import tungtt.Console.Console;
import tungtt.Envelope.Contexts.EnvelopeMetaContext;
import tungtt.Envelope.Contexts.EnvelopeSecureContext;

public final class HeartbeatService {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "heartbeat-thread");
        t.setDaemon(true);
        return t;
    });

    private HeartbeatService() {
    }

    public static void start(long intervalSeconds) {

        String hostId = String.valueOf(ConfigService.HOST_ID());

        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                EnvelopeMetaContext envelopeMetacontext = new EnvelopeMetaContext(
                        hostId,
                        MetaRespository.hostFrom(),
                        MetaRespository.hostVersion(),
                        Console.now());

                EnvelopeSecureContext envelopeSecurecontext = new EnvelopeSecureContext("[coming soon]",
                        "[coming soon]");

                CommandContext ctx = new CommandContext(
                        hostId,
                        envelopeMetacontext,
                        envelopeSecurecontext,
                        new JsonObject(),
                        new JsonObject());

                HeartbeatPublisher publisher = new HeartbeatPublisher(
                        ConfigService.HEALTH_TOPIC(),
                        CommandService.getInstance());

                publisher.publish(
                        ctx,
                        "sensor",
                        SensorService.shortedSensorData());

            } catch (Exception e) {
                // không throw để scheduler chết
                e.printStackTrace();
            }

        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public static void stop() {
        SCHEDULER.shutdownNow();
    }
}
