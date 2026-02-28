package tungtt.Broker.Mqtt.Interfaces;

public interface MqttMessageInterface {
    
    void onMessage(String topic, byte[] payload);
}