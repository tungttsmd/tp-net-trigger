package com.tpservers.CommandHandler.Modules.Sensor;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.EnvelopPublisher.HeartbeatPublisher;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;

import com.google.gson.JsonObject;

import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.SensorService;
import com.tpservers.Services.Facade.CommandService;

@ModuleAnnotation
public final class SensorHeartbeatModule implements CommandModule {

    @Override
    public String title() {
        return "sensor";
    }

    @Override
    public String command() {
        return "heartbeat";
    }

    @Override
    public void handle(CommandContext ctx) {


        HeartbeatPublisher heartbeatPublisher = new HeartbeatPublisher(
            ConfigService.HEALTH_TOPIC(),
            CommandService.getInstance()
        );
        
        JsonObject shw = SensorService.shortedSensorData();

        heartbeatPublisher.publish(
            ctx,
            "sensor",
            shw);
    }
}
