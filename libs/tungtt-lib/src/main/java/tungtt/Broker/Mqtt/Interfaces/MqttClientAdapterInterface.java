package tungtt.Broker.Mqtt.Interfaces;

import tungtt.Broker.Mqtt.Configs.MqttInit;
import tungtt.Broker.Mqtt.Configs.MqttOption;
import tungtt.Broker.Mqtt.Interfaces.MqttMessageInterface;

public interface MqttClientAdapterInterface  {

    void connect(MqttInit configs, MqttOption options) throws Exception;

    void disconnect() throws Exception;

    boolean isConnected();

    void subscribe(String topic, int qos) throws Exception;

    void setMessageHandler(MqttMessageInterface handler);

    void publish(String topic, byte[] payload, int qos) throws Exception;
}
