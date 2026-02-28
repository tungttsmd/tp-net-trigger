package tungtt.Broker.Mqtt.Core;

import tungtt.Broker.Mqtt.Configs.MqttInit;
import tungtt.Broker.Mqtt.Facade.MqttFacade;

public final class MqttCore {

    private MqttCore() {}

    public static MqttFacade start(MqttInit config) throws Exception {
        
        MqttFacade service = new MqttFacade();
        service.connect(config);
        return service;
    }
}
