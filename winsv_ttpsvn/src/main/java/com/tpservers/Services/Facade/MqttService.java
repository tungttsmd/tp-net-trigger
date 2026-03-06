package com.tpservers.Services.Facade;

import java.util.Set;

import com.tpservers.Repositories.MetaRespository;
import tungtt.Broker.Mqtt.Core.MqttCore;
import tungtt.Broker.Mqtt.Configs.MqttInit;
import tungtt.Broker.Mqtt.Configs.MqttOption;
import tungtt.Broker.Mqtt.Facade.MqttFacade;
import tungtt.Broker.Mqtt.Interfaces.MqttMessageInterface;
import tungtt.Console.Console;

public final class MqttService {

    private MqttService() {
    }

    private static class Holder {
        static String CLIENT_ID;
        static MqttFacade mqtt;

        // SUB TOPICS INPUT
        static final Set<String> SUB_TOPICS = Set.of(
                ConfigService.CONTROL_TOPIC());
    }

    public static void start() {

        Holder.CLIENT_ID = ConfigService.HOST_FROM_PREFIX() + "-" + ConfigService.HOST_ID() + "-"
                + MetaRespository.hostHwid();

        MqttInit config = new MqttInit(
                ConfigService.MQTT_BROKER_URL(),
                Holder.CLIENT_ID);

        String username = ConfigService.MQTT_OPT_USERNAME();

        Console.info("MQTT_OPT_USERNAME read from .env: " + (username != null && !username.isBlank() ? "Yes" : "No"));
        Console.info("MQTT_OPT_PASSWORD read from .env: "
                + (ConfigService.MQTT_OPT_PASSWORD() != null && !ConfigService.MQTT_OPT_PASSWORD().isBlank() ? "Yes"
                        : "No"));

        MqttOption options;

        if (username != null && !username.isBlank()) {
            options = new MqttOption(
                    1,
                    false,
                    30,
                    60,
                    true,
                    username,
                    ConfigService.MQTT_OPT_PASSWORD());
            Console.info("MQTT Auth mode is: login (user=" + username + ")");

        } else {
            Console.info("MQTT Auth mode is: anonymous");
            options = MqttOption.defaults();
        }

        Console.line();

        while (true) {
            try {
                Holder.mqtt = MqttCore.start(config, options);
                break;
            } catch (Exception e) {
                Console.error("Failed to connect MQTT, retry in 5s: " + e.getMessage());
                try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        for (String subTopic : Holder.SUB_TOPICS) {
            try {
                Holder.mqtt.subscribe(subTopic);
            } catch (Exception e) {
                Console.error("Failed to subscribe topic >> lost connect to MQTT broker: " + subTopic + " - " + e.getMessage());
            }
        }

        Console.info("MQTT booted — Host Identify (mqtt client_id): " + Holder.CLIENT_ID);
        Console.info("SUB topics:");
        for (String t : Holder.SUB_TOPICS)
            Console.info("  <- " + t);
        Console.info("PUB topics:");
        Console.info("  -> " + ConfigService.RUNTIME_TOPIC());
        Console.info("  -> " + ConfigService.PROFILE_TOPIC());
        Console.info("  -> " + ConfigService.HEALTH_TOPIC());
        Console.info("  -> " + ConfigService.SENSOR_TOPIC());
        Console.info("  -> " + ConfigService.SYSTEM_TOPIC());
        Console.info("  -> " + ConfigService.SIGNAL_TOPIC());
        Console.line();
    }

    public static void onMessage(MqttMessageInterface handler) {

        if (Holder.mqtt == null) {
            Console.error("Mqtt broker is not started");
            return;
        }

        try {
            Holder.mqtt.onMessage(handler);
        } catch (Exception e) {
            Console.error("SUB fail: lost connect to MQTT broker: " + e.getMessage());
        }
    }

    public static void publish(String topic, String payload, int qos) {

        if (Holder.mqtt == null) {
            Console.error("Mqtt broker is not started");
            return;
        }

        try {
            Holder.mqtt.publish(topic, payload);
        } catch (Exception e) {
            Console.error("PUB fail: lost connect to MQTT broker: " + e.getMessage());
        }
    }

    public static String clientId() {

        if (Holder.CLIENT_ID == null) {
            Console.error("Mqtt client chưa được khởi tạo");
            throw new RuntimeException("MqttService is not booted");
        }
        try {
            return Holder.CLIENT_ID;
        } catch (Exception e) {
            Console.error("Get client_id fail: lost connect to MQTT broker: " + e.getMessage());
            return null;
        }
    }
}
