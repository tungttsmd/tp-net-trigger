package com.tpservers.Services.Facade;

import org.eclipse.paho.client.mqttv3.IMqttClient;

import com.google.gson.JsonObject;
import com.tpservers.Core.MqttCore;
import com.tpservers.Core.MqttCore.MessageHandler;

public final class MqttService {

    private MqttService() {
    }

    private static class Holder {

        final static String MQTT_CLIENT_ID = buildClientId();

        final static JsonObject SUB_TOPIC = new JsonObject();
        final static JsonObject PUB_TOPIC = new JsonObject();

        static {
            SUB_TOPIC.addProperty("control", ConfigService.CONTROL_TOPIC());
            PUB_TOPIC.addProperty("runtime", ConfigService.RUNTIME_TOPIC());
            PUB_TOPIC.addProperty("sensor", ConfigService.SENSOR_TOPIC());
            PUB_TOPIC.addProperty("profile", ConfigService.PROFILE_TOPIC());
            PUB_TOPIC.addProperty("system", ConfigService.SYSTEM_TOPIC());
            PUB_TOPIC.addProperty("health", ConfigService.HEALTH_TOPIC());
            PUB_TOPIC.addProperty("signal", ConfigService.SIGNAL_TOPIC());

        }

        final static MqttService INSTANCE = new MqttService();
    }

    public static MqttService getInstance() {
        return Holder.INSTANCE;
    }

    /* ================= CONNECT ================= */

    public static void connect() {
        int maxRetries = 3;
        int retries = maxRetries;
        while (retries > 0) {
            retries--;
            try {
                if (MqttService.mqttClient() != null) {
                    break;
                }

                MqttCore.connect(clientId());

                ConsoleService.info("MQTT connected with clientId: " + clientId());

                MqttService.resubscribe();

            } catch (Exception e) {

                ConsoleService.error("Connect MQTT failed, attempts left: " + retries + " - " + e.getMessage());
                if (retries == 0) {

                    ConsoleService.error("MQTT connection failed after multiple attempts.");
                }
                try {

                    ConsoleService.info("[" + retries + "/" + maxRetries + "] Retrying MQTT connection...");
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {

                    ConsoleService.info("[" + retries + "/" + maxRetries + "] Interrupted trying MQTT connection...");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /* ================= GETTER ================= */

    public static String clientId() {

        if (Holder.MQTT_CLIENT_ID == null) {
            return buildClientId();
        }

        return Holder.MQTT_CLIENT_ID;
    }

    public static JsonObject subTopic() {

        return Holder.SUB_TOPIC;
    }

    public static JsonObject pubTopic() {

        return Holder.PUB_TOPIC;
    }

    /* =============== CLIENT ID BUILD =============== */

    private static String buildClientId() {

        return ConfigService.HOST_FROM_PREFIX()
                + "-"
                + ConfigService.HOST_ID()
                + "-"
                + HardwareService.hwHwid();
    }

    /* ================= MQTT CLIENT ================= */
    public static IMqttClient mqttClient() {

        return MqttCore.getMqttClient();
    }

    /* ============== MESSAGE HANDLE ============= */

    public static void messageHandler(MessageHandler handler) {

        if (mqttClient() != null) {

            if (handler == null) {

                ConsoleService.error("Message handler cannot be null");
                return;
            }
            MqttCore.setMessageHandler(handler);
        } else {
            ConsoleService.error("MQTT client is not initialized");
        }
    }

    /* ================= PUBLISH ================= */

    public static void publish(String topic, String message, int qos) {
        try {
            if (mqttClient() == null) {
                ConsoleService.info("MQTT has not connected yet");
                return;
            }

            try {
                MqttCore.publish(topic, message, qos);
            } catch (Exception e) {
                ConsoleService.error("Publish failed: " + e.getMessage());
            }
        } catch (Exception e) {
            ConsoleService.error("Publish failed: " + e.getMessage());
        }
    }

    /* ================= SUBSCRIBE ================= */

    private static void resubscribe() {

        ConsoleService.info("Resubscribing init topics...");

        MqttService.subscribe();

        ConsoleService.info("Resubscribing finished");
    }

    private static void subscribe() {

        if (mqttClient() == null) {
            ConsoleService.info("MQTT has not connected yet");
            return;
        }

        try {

            for (String key : Holder.SUB_TOPIC.keySet()) {

                var el = Holder.SUB_TOPIC.get(key);
                if (el == null || !el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString()) {

                    ConsoleService.error("Invalid topic config: " + key);
                    continue;
                }
                String topic = el.getAsString();

                MqttCore.subscribe(topic, 0);

                ConsoleService.info("Subscribed to topic: " + topic);
            }
            ConsoleService.info("MQTT subscribed init topics");
        } catch (Exception e) {
            ConsoleService.error("Subscribe failed: " + e.getMessage());
        }
    }

}
