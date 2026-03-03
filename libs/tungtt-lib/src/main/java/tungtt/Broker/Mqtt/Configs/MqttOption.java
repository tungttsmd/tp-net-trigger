package tungtt.Broker.Mqtt.Configs;

public record MqttOption(
        int fallbackQos,
        boolean cleanSession,
        int connectionTimeout,
        int keepAlive,
        boolean autoReconnect,
        String username,
        String password
) {
        public boolean hasCredentials() {
                return username != null && !username.isBlank();
        }

        public static MqttOption defaults() {

                return new MqttOption(
                        1,      // fallbackQos
                        false,  // cleanSession
                        60,     // connectionTimeout
                        60,     // keepAlive
                        true,   // autoReconnect
                        null,   // username
                        null    // password
                );
        }
}
