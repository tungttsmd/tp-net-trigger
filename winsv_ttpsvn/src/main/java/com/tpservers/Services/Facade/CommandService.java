package com.tpservers.Services.Facade;

import tungtt.Envelope.Modules.HeartbeatEnvelope;
import tungtt.Envelope.Modules.ReportEnvelope;
import tungtt.Envelope.Modules.CommandEnvelope;
import tungtt.Envelope.Modules.SignalEnvelope;

import tungtt.Handler.CommandHandler.PublishHooker.CommandPublishHooker;
import tungtt.Handler.CommandHandler.Configs.CommandConfig;
import tungtt.Handler.CommandHandler.Core.CommandHandlerCore;

import tungtt.Console.Console;
import tungtt.Console.JsonConsole;

import com.tpservers.Services.Facade.MqttService;
import com.tpservers.Services.Facade.ConfigService;

public class CommandService implements CommandPublishHooker {

    private CommandService() {}

    private static class Holder {

        private static boolean started = false;
        private static final CommandService INSTANCE = new CommandService();
    }

    public static CommandService getInstance() {

        return Holder.INSTANCE;
    }

    public void boot(CommandConfig config) {

        if (Holder.started == true) {
            Console.error("CommandService already started");
            return;
        }

        CommandHandlerCore.boot(config);
        Holder.started = true;
    }

    @Override
    public Runnable heartbeatPublish(HeartbeatEnvelope envelope, String publishTopic) {
        return () -> {
            MqttService.publish(
                publishTopic,
                JsonConsole.toJson(envelope.build()),
                1
            );
        };
    }

    @Override
    public Runnable reportPublish(ReportEnvelope envelope, String publishTopic) {
        return () -> {
            MqttService.publish(
                publishTopic,
                JsonConsole.toJson(envelope.build()),
                1
            );
        };
    }

    @Override
    public Runnable commandPublish(CommandEnvelope envelope, String publishTopic) {
        return () -> {
            MqttService.publish(
                publishTopic,
                JsonConsole.toJson(envelope.build()),
                1
            );
        };
    }

    @Override
    public Runnable signalPublish(SignalEnvelope envelope, String publishTopic) {
        return () -> {
            MqttService.publish(
                publishTopic,
                JsonConsole.toJson(envelope.build()),
                1
            );
        };
    }
}
