package com.tpservers.CommandHandler.Modules.Profile;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.ShellBuilder.CommandShellBuilder;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.EnvelopPublisher.SignalPublisher;
import tungtt.Handler.CommandHandler.EnvelopPublisher.ReportPublisher;

import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.HardwareService;
import com.tpservers.Services.Facade.CommandService;

import com.google.gson.JsonObject;

@ModuleAnnotation
public final class ProfileHardwareCpuModule implements CommandModule {


    @Override
    public String title() {
        return "profile";
    }

    @Override
    public String command() {
        return "hardware-cpu";
    }

    @Override
    public void handle(CommandContext ctx) {

        SignalPublisher signalPublisher = new SignalPublisher(
            ConfigService.SIGNAL_TOPIC(),
            CommandService.getInstance()
        );

        ReportPublisher reportPublisher = new ReportPublisher(
            ConfigService.PROFILE_TOPIC(),
            CommandService.getInstance()
        );

        JsonObject hwc = HardwareService.getCpus();

        reportPublisher.publish(
            title(),
            command(),
            ctx,
            hwc);

        signalPublisher.publish(
            ConfigService.SIGNAL_TARGET_KEY(),
            title(),
            command(),
            ctx,
            new JsonObject());
    }
}
