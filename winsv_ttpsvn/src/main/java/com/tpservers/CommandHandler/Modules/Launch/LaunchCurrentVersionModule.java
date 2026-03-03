package com.tpservers.CommandHandler.Modules.Launch;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.EnvelopPublisher.SignalPublisher;
import tungtt.Handler.CommandHandler.EnvelopPublisher.ReportPublisher;

import tungtt.Console.Console;
import com.tpservers.Services.Facade.CommandService;

import com.google.gson.JsonObject;
import com.tpservers.Services.Facade.ConfigService;

@ModuleAnnotation
public final class LaunchCurrentVersionModule implements CommandModule {

    @Override
    public String title() {
        return "launch";
    }

    @Override
    public String command() {
        return "current-version";
    }

    @Override
    public void handle(CommandContext ctx) {
        try {

            SignalPublisher signalPublisher = new SignalPublisher(
                ConfigService.SIGNAL_TOPIC(),
                CommandService.getInstance()
            );

            ReportPublisher reportPublisher = new ReportPublisher(
                ConfigService.RUNTIME_TOPIC(),
                CommandService.getInstance()
            );

            JsonObject runtime = new JsonObject();
            runtime.addProperty("current-version", ConfigService.HOST_VERSION());
            runtime.addProperty("host-from-prefix", ConfigService.HOST_FROM_PREFIX());
            runtime.addProperty("host-id", ConfigService.HOST_ID());

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
                runtime);

        } catch (Exception e) {
            Console.error(e.getMessage());
        }
    }
}
