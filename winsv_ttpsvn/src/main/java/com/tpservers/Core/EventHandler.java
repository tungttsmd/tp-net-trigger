package com.tpservers.Core;

import tungtt.Handler.CommandHandler.Dispatchers.CommandDispatcher;
import tungtt.Handler.CommandHandler.Configs.CommandConfig;

import com.tpservers.Services.Facade.CommandService;
import com.tpservers.Services.Facade.MqttService;
import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Core.PoolCore;

import tungtt.Broker.Mqtt.Interfaces.MqttMessageInterface;
import tungtt.Console.Console;

public final class EventHandler {
    private EventHandler() {
    }

    private static class Holder {
        static boolean started = false;

        final static EventHandler INSTANCE = new EventHandler();
    }

    public static EventHandler getInstance() {
        return Holder.INSTANCE;
    }

    public static void boot() {

        if (Holder.started) {
            Console.info("EventHanlder already started");
            return;
        }

        try {
            MqttService.onMessage((subscribeTopic, rawPayload) -> {

                String message = new String(rawPayload);

                PoolCore.submitJob("mqtt-msg", () -> {
                    CommandConfig config = new CommandConfig(
                        String.valueOf(ConfigService.HOST_ID()),
                        ConfigService.HOST_FROM_PREFIX(),
                        ConfigService.HOST_VERSION(),
                        "com.tpservers.CommandHandler.Modules"
                    );

                    CommandService
                        .getInstance()
                        .boot(config);

                    CommandDispatcher
                        .getInstance(config)
                        .handle(message);
                });
            });
        } catch (Exception e) {

            Console.error("MQTT boot error: " + e.getMessage());
            e.printStackTrace();
        }

        Console.info("EventHanlder started");
        Holder.started = true;
    }
}