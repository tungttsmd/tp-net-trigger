package com.tpservers.CommandHandler.Modules.Power;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;
import tungtt.Handler.CommandHandler.ShellBuilder.CommandShellBuilder;

import com.tpservers.Models.WakeOnLanSender;
import com.tpservers.Models.IpMacCaching;
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
            String ip = null;

            if (commandContext != null && commandContext.startsWith(prefix)) {
                ip = commandContext.substring(prefix.length());
            }

            if (ip == null) {
                throw new IllegalArgumentException("Missing IP in command_context");
            }

            String mac = IpMacCaching.resolveMac(ip);

            if (mac == null) {
                throw new IllegalStateException(
                    "Cannot resolve MAC for IP " + ip
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
                "WOL sent: ip=" + ip + ", mac=" + mac
            );

        } catch (Exception e) {
            Console.error("PowerOn failed: " + e.getMessage());
        }
    }
}
