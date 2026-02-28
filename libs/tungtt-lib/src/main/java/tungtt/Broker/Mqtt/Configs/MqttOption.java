package tungtt.Broker.Mqtt.Configs;

public record MqttOption(
        int fallbackQos,
        boolean cleanSession,
        int connectionTimeout,
        int keepAlive,
        boolean autoReconnect
) {
        public static MqttOption defaults() {
                
                return new MqttOption(
                        1,      // fallbackQos
                        false,  // cleanSession
                        60,     // connectionTimeout
                        60,     // keepAlive
                        true    // autoReconnect
                );
        }
}
