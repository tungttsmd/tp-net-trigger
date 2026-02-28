package tungtt.Broker.Mqtt.Configs;

public record MqttInit(
        String brokerUrl,
        String clientId
) {}
