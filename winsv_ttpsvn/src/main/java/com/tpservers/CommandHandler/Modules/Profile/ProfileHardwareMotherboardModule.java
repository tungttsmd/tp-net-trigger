package com.tpservers.CommandHandler.Modules.Profile;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.EnvelopPublisher.ReportPublisher;
import tungtt.Handler.CommandHandler.EnvelopPublisher.SignalPublisher;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;

import com.google.gson.JsonObject;

import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.HardwareService;
import com.tpservers.Services.Facade.CommandService;

@ModuleAnnotation
public final class ProfileHardwareMotherboardModule implements CommandModule {

    @Override
    public String title() {
        return "profile";
    }

    @Override
    public String command() {
        return "hardware-motherboard";
    }

    @Override
    public void handle(CommandContext ctx) {

        ReportPublisher reportPublisher = new ReportPublisher(
            ConfigService.PROFILE_TOPIC(),
            CommandService.getInstance()
        );

        SignalPublisher signalPublisher = new SignalPublisher(
            ConfigService.SIGNAL_TOPIC(),
            CommandService.getInstance()
        );
        
        JsonObject hwm = HardwareService.getMotherboard();
        
        reportPublisher.publish(
            title(),
            command(),
            ctx,
            hwm);

        signalPublisher.publish(
            ConfigService.SIGNAL_TARGET_KEY(),
            title(),
            command(),
            ctx,
            new JsonObject());
    }
}
