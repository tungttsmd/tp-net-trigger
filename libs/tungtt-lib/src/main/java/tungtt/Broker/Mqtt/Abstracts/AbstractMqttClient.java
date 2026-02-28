package tungtt.Broker.Mqtt.Abstracts;

import java.util.*;

import tungtt.Broker.Mqtt.Interfaces.MqttClientAdapterInterface;
import tungtt.Broker.Mqtt.Interfaces.MqttMessageInterface;

public abstract class AbstractMqttClient implements MqttClientAdapterInterface {

    protected final Map<String, Integer> subscribedTopics =
            Collections.synchronizedMap(new HashMap<>());

    protected MqttMessageInterface handler;

    @Override
    public void setMessageHandler(MqttMessageInterface handler) {
        this.handler = handler;
    }

    protected void onMessageInternal(String topic, byte[] payload) {
        if (handler != null) {
            handler.onMessage(topic, payload);
        }
    }

    protected void rememberTopic(String topic, int qos) {
        subscribedTopics.put(topic, qos);
    }

    protected Map<String, Integer> topics() {
        return subscribedTopics;
    }
}
