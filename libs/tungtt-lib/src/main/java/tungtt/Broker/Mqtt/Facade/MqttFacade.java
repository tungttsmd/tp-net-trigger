package tungtt.Broker.Mqtt.Facade;

import java.nio.charset.StandardCharsets;

import tungtt.Broker.Mqtt.Configs.MqttInit;
import tungtt.Broker.Mqtt.Implements.MqttClientImplement;
import tungtt.Broker.Mqtt.Interfaces.MqttClientAdapterInterface;
import tungtt.Broker.Mqtt.Interfaces.MqttMessageInterface;

public final class MqttFacade {

    private final MqttClientAdapterInterface client;

    public MqttFacade() {
        
        this.client = new MqttClientImplement();
    }

    public void connect(MqttInit config) throws Exception {
        client.connect(config, null);
    }

    public void subscribe(String topic) throws Exception {
        client.subscribe(topic, 1);
    }

    public void publish(String topic, String payload) throws Exception {
        client.publish(topic, payload.getBytes(StandardCharsets.UTF_8), 1);
    }

    public void onMessage(MqttMessageInterface handler) {
        client.setMessageHandler(handler);
    }

    public void shutdown() throws Exception {
        client.disconnect();
    }
}
