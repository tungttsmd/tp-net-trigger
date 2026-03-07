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

import com.google.gson.JsonObject;


@ModuleAnnotation
public final class ProfileHardwareDiskModule implements CommandModule {

    @Override
    public String title() {
        return "profile";
    }

    @Override
    public String command() {
        return "hardware-disk";
    }

    @Override
    public void handle(CommandContext ctx) {

        JsonObject hwd = HardwareService.getDisks();
        ReportPublisher reportPublisher = new ReportPublisher(
            ConfigService.PROFILE_TOPIC(),
            CommandService.getInstance()
        );

        SignalPublisher signalPublisher = new SignalPublisher(
            ConfigService.SIGNAL_TOPIC(),
            CommandService.getInstance()
        );

        reportPublisher.publish(
            title(),
            command(),
            ctx,
            hwd);

        signalPublisher.publish(
            ConfigService.SIGNAL_TARGET_KEY(),
            title(),
            command(),
            ctx,
            new JsonObject());
    }
}
