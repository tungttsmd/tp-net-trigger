package com.tpservers.CommandHandler.Modules.Power;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;
import tungtt.Handler.CommandHandler.ShellBuilder.CommandShellBuilder;

import com.tpservers.Models.WakeOnLanSender;
import com.tpservers.Models.WakeOnLanMacSolver;
import tungtt.Console.Console;
import tungtt.Console.JsonConsole;

import java.lang.String;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

@ModuleAnnotation
public final class PowerOnModule implements CommandModule {

    @Override
    public String title() { return "power"; }

    @Override
    public String command() { return "power-on"; }

    @Override
    public void handle(CommandContext ctx) {

        try {
            JsonObject data = ctx.data();

            String commandContext =
                data.get("command_context").getAsString();

            String prefix = "wake-on-lan-";
            String targetIp = null;

            if (commandContext != null && commandContext.startsWith(prefix)) {
                targetIp = commandContext.substring(prefix.length());
            }

            if (targetIp == null) {
                throw new IllegalArgumentException("Missing target IP in command_context");
            }

            String mac = WakeOnLanMacSolver.resolveMac(targetIp);

            if (mac == null) {
                throw new IllegalStateException(
                    "Cannot resolve MAC for IP " + targetIp
                );
            }

            String broadcast =
                data.has("broadcast")
                    ? data.get("broadcast").getAsString()
                    : "255.255.255.255";

            CommandShellBuilder.on(() -> {
                try {
                    WakeOnLanSender.send(mac, broadcast);
                } catch (Exception e) {
                    Console.error("PowerOn failed: " + e.getMessage());
                }
            });

            Console.info(
                "WOL sent: target ip=" + targetIp + ", mac=" + mac
            );

        } catch (Exception e) {
            Console.error("PowerOn failed: " + e.getMessage());
        }
    }
}
