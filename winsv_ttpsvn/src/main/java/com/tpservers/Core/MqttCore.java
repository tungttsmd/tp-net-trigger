package com.tpservers.Core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.tpservers.Services.Facade.ConsoleService;

public class MqttCore implements MqttCallbackExtended {

    private IMqttClient client;
    private String clientId;
    private final List<String> subscribedTopic = Collections.synchronizedList(new ArrayList<>());
    private MessageHandler handler;

    private MqttCore() {
    }

    /* ================= SINGLETON ================= */

    private static class Holder {

        private static String prop(String key) {
            String v = System.getProperty(key);
            if (v == null || v.isBlank()) {
                throw new IllegalStateException("Missing system property: " + key);
            }
            return v;
        }

        private static final String TCP_BROKER_URL = "tcp://" + prop("MQTT_SERVER_HOST") + ":" +
                Integer.parseInt(prop("MQTT_SERVER_PORT"));

        private static final int OPT_FALLBACK_QOS = Integer.parseInt(prop("MQTT_OPT_FALLBACK_QOS"));

        private static final boolean OPT_CLEAN_SESSION = Boolean.parseBoolean(prop("MQTT_OPT_CLEAN_SESSION"));

        private static final int OPT_CONNECTION_TIMEOUT = Integer.parseInt(prop("MQTT_OPT_CONNECTION_TIMEOUT"));

        private static final int OPT_KEEP_ALIVE = Integer.parseInt(prop("MQTT_OPT_KEEP_ALIVE"));

        private static final MqttCore INSTANCE = new MqttCore();
    }

    public static MqttCore getInstance() {
        return Holder.INSTANCE;
    }

    /* ================= CORE ================= */

    public static void connect(String clientId) throws MqttException {

        if (Holder.INSTANCE.clientId != null && !Holder.INSTANCE.clientId.equals(clientId)) {
            throw new IllegalStateException(
                    "MQTT clientId already initialized: " + Holder.INSTANCE.clientId);
        }

        if (Holder.INSTANCE.client != null && Holder.INSTANCE.client.isConnected()) {
            return;
        }

        Holder.INSTANCE.clientId = clientId;

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(Holder.OPT_CLEAN_SESSION);
        options.setConnectionTimeout(Holder.OPT_CONNECTION_TIMEOUT);
        options.setKeepAliveInterval(Holder.OPT_KEEP_ALIVE);

        Holder.INSTANCE.client = new MqttClient(Holder.TCP_BROKER_URL, clientId, new MemoryPersistence());
        Holder.INSTANCE.client.setCallback(Holder.INSTANCE);
        Holder.INSTANCE.client.connect(options);

        ConsoleService.info("MQTT connected as " + clientId);
    }

    public static void subscribe(String topic, int qos) throws MqttException {

        if (Holder.INSTANCE.client == null || !Holder.INSTANCE.client.isConnected()) {
            throw new IllegalStateException("MQTT not connected");
        }

        if (!Holder.INSTANCE.subscribedTopic.contains(topic)) {
            Holder.INSTANCE.subscribedTopic.add(topic);
            Holder.INSTANCE.client.subscribe(topic, qos);
            ConsoleService.info("Subscribed: " + topic);
        }
    }

    public static void publish(String topic, String payload, int qos) throws MqttException {

        if (Holder.INSTANCE.client == null || !Holder.INSTANCE.client.isConnected()) {
            ConsoleService.error("MQTT not connected");
            throw new IllegalStateException("MQTT not connected");
        }

        if (payload == null) {
            ConsoleService.error("Payload cannot be null");
            throw new IllegalArgumentException("Payload cannot be null");
        }

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(Math.max(qos, 1));
        message.setRetained(false);

        Holder.INSTANCE.client.publish(topic, message);

        ConsoleService.info("Published to " + topic);
    }

    public static void setMessageHandler(MessageHandler handler) {
        Holder.INSTANCE.handler = handler;
    }

    /* ================= CALLBACK ================= */

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("THREAD = " + Thread.currentThread().getName());

        if (Holder.INSTANCE.handler != null) {
            Holder.INSTANCE.handler.onMessage(topic, new String(message.getPayload()));
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        ConsoleService.info((reconnect ? "Reconnected" : "Connected") + " to " + serverURI);

        if (reconnect && Holder.INSTANCE.client != null && Holder.INSTANCE.client.isConnected()) {
            synchronized (Holder.INSTANCE.subscribedTopic) {
                for (String topic : Holder.INSTANCE.subscribedTopic) {
                    try {
                        Holder.INSTANCE.client.subscribe(topic, Holder.OPT_FALLBACK_QOS);
                    } catch (MqttException e) {
                        ConsoleService.error("Resubscribe failed: " + topic);
                    }
                }
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        ConsoleService.error("MQTT connection lost: " + cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // no-op
    }

    /* ================= INTERFACE ================= */

    public interface MessageHandler {
        void onMessage(String topic, String payload);
    }

    public static IMqttClient getMqttClient() {
        return Holder.INSTANCE.client;
    }
}
