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
public final class LaunchUpdateModule implements CommandModule {

    @Override
    public String title() {
        return "launch";
    }

    @Override
    public String command() {
        return "launch-update";
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
            runtime.addProperty("status", "update received");

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

            System.exit(80); // Trả cho Launch exit code 80 để Launch tự update

        } catch (Exception e) {
            Console.error(e.getMessage());
        }
    }
}
