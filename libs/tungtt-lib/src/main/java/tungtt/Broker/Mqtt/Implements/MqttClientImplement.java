package tungtt.Broker.Mqtt.Implements;

import java.util.Map;
import java.lang.Exception;

import tungtt.Console.Console;
import tungtt.Broker.Mqtt.Abstracts.AbstractMqttClient;
import tungtt.Broker.Mqtt.Configs.MqttInit;
import tungtt.Broker.Mqtt.Configs.MqttOption;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

public final class MqttClientImplement
        extends AbstractMqttClient
        implements MqttCallbackExtended {

    private IMqttClient client;

    private MqttOption options;

    /* ============ BROKER CONNECTION ============= */
    
    @Override
    public void connect(MqttInit configs, MqttOption options) throws Exception {

        this.client = new MqttClient(
                configs.brokerUrl(),
                configs.clientId(),
                new MemoryPersistence()
        );

        if (options == null) {
            this.options = MqttOption.defaults();
        } else {
            this.options = options;
        };

        MqttConnectOptions opt = new MqttConnectOptions();

        opt.setAutomaticReconnect(this.options.autoReconnect());
        opt.setCleanSession(this.options.cleanSession());
        opt.setConnectionTimeout(this.options.connectionTimeout());
        opt.setKeepAliveInterval(this.options.keepAlive());

        if (this.options.hasCredentials()) {
            opt.setUserName(this.options.username());
            opt.setPassword(this.options.password().toCharArray());
        }

        client.setCallback(this);
        client.connect(opt);
    }

    @Override
    public void disconnect() throws Exception {

        try {

            client.disconnect();
        } catch (Exception e) {
            throw new Exception("Failed to disconnect MQTT client", e);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /* ============ BROKER SUB/PUB METHODS ============= */

    @Override
    public void subscribe(String subTopic, int qos) throws Exception {

        int q = this.normalizeQos(qos);
        client.subscribe(subTopic, q);
        rememberTopic(subTopic, q);
    }


    @Override
    public void publish(String pubTopic, byte[] payload, int qos) throws Exception {
        
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(this.normalizeQos(qos));
        client.publish(pubTopic, msg);
    }

    /* ============ PAHO COMMITED CALLBACK THESE  ============ */

    @Override
    public void messageArrived(String subTopic, MqttMessage message) {

        onMessageInternal(subTopic, message.getPayload());
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

        for (Map.Entry<String, Integer> e : topics().entrySet()) {

            try {
                client.subscribe(
                    e.getKey(),
                    normalizeQos(e.getValue())
                );
            } catch (Exception exc) {
                Console.error("connectComplete lib error: " + exc);
                throw new RuntimeException("connectComplete error", exc);
            }
        }
    }

    @Override public void connectionLost(Throwable cause) {}
    @Override public void deliveryComplete(IMqttDeliveryToken token) {}

    /* ============= HELPER ============ */

    /* Helper ngăn chặn lib nhận Qos = 0 (thường gây ra lỗi, tôi quyết định bỏ qua 0) */
    private int normalizeQos(int qos) {

        if (qos < 1) return 1;
        if (qos > 2) return 2;
        return qos;
    }
}
