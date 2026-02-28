package com.tpservers.CommandHandler.Modules.Power;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.ShellBuilder.CommandShellBuilder;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.EnvelopPublisher.SignalPublisher;
import tungtt.Handler.CommandHandler.EnvelopPublisher.ReportPublisher;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;
import tungtt.Console.Console;
import com.tpservers.Services.Facade.CommandService;

import com.tpservers.Services.Facade.ConfigService;
import com.google.gson.JsonObject;


@ModuleAnnotation
public final class PowerResetModule implements CommandModule {

    @Override
    public String title() {
        return "power";
    }

    @Override
    public String command() {
        return "power-reset";
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
            runtime.addProperty("status", "power reset received");

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

            CommandShellBuilder.reset();

        } catch (Exception e) {
            Console.error(e.getMessage());
        }
    }
}
