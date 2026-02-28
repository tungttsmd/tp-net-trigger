package com.tpservers.CommandHandler.Modules.Health;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import com.tpservers.Services.Facade.ConfigService;
import com.tpservers.Services.Facade.CommandService;

import com.google.gson.JsonObject;

import tungtt.Handler.CommandHandler.EnvelopPublisher.HeartbeatPublisher;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;

import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;

@ModuleAnnotation
public final class HealthHeartbeatModule implements CommandModule {

    @Override
    public String title() {
        return "health";
    }

    @Override
    public String command() {
        return "heartbeat";
    }

    @Override
    public void handle(CommandContext ctx) {

        HeartbeatPublisher heartbeatPublisher = new HeartbeatPublisher(
                ConfigService.HEALTH_TOPIC(),
                CommandService.getInstance());

        JsonObject h = new JsonObject();

        h.addProperty("status", "alive");

        heartbeatPublisher.publish(
                ctx,
                "health",
                h);
    }
}
