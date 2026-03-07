package com.tpservers.CommandHandler.Modules.Sensor;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.EnvelopPublisher.ReportPublisher;
import tungtt.Handler.CommandHandler.EnvelopPublisher.SignalPublisher;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;

import com.google.gson.JsonObject;

import com.tpservers.Services.Facade.CommandService;
import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.SensorService;

@ModuleAnnotation
public final class SensorTemperatureModule implements CommandModule {

    @Override
    public String title() {
        return "sensor";
    }

    @Override
    public String command() {
        return "temperatures";
    }

    @Override
    public void handle(CommandContext ctx) {

        SignalPublisher signalPublisher = new SignalPublisher(
            ConfigService.SENSOR_TOPIC(),
            CommandService.getInstance()
        );

        ReportPublisher reportPublisher = new ReportPublisher(
            ConfigService.SENSOR_TOPIC(),
            CommandService.getInstance()
        );
            

        JsonObject thw = SensorService.temperatureFromWebserver();

        signalPublisher.publish(
            ConfigService.SIGNAL_TARGET_KEY(),
            title(),
            command(),
            ctx,
            new JsonObject());

        reportPublisher.publish(
            title(),
            command(),
            ctx,
            thw);
    }
}
