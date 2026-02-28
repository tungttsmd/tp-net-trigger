package com.tpservers.Services;

import com.tpservers.Services.Facade.HeartbeatService;
import com.tpservers.Services.Facade.MqttService;
import com.tpservers.Services.Facade.WorkerService;

import com.tpservers.Services.Facade.ConfigService;
import tungtt.Console.JsonConsole;
import tungtt.Console.Console;

public final class Service {

    private Service() {
    }

    private static class Holder {

        static boolean started = false;
        static final Service INSTANCE = new Service();
    }

    public static Service getInstance() {
        return Holder.INSTANCE;
    }

    public static void boot() {

        if (Holder.started == true) {
            Console.error("Services already started");
            return;
        }

        Console.info("Booting services...");
        Console.line();


        /* ========== TURN MQTT SERVICE ON ============ */
        try {
            MqttService.connect();
            
            Console.info("MQTT Client ID: " + MqttService.clientId() + "\n" +
                    "[1/2] MQTT Service booted successfully");
        } catch (Exception e) {
            Console.error("[1/2] MQTT Service booted failed");
        }
        Console.line();

        /* ========== TURN WORKER SERVICE ON ============ */
        try {
            WorkerService.start();
            Console.success("Worker pool count: " + WorkerService.getWorkerCount() + "\n" +
                    "[2/2] Worker pool service booted successfully");
        } catch (Exception e) {
            Console.error("[2/2] Worker pool service booted failed");
        }

        Console.line();

        /* ========== TURN HEARTBEAT ============ */
        HeartbeatService.start(12);

        /* ========== INFOMATION ============ */
        Console.info("Services booted successfully");
        Console.line();
        Console.info("HOST ID: " + ConfigService.HOST_ID());
        Console.info("MQTT Client ID: " + MqttService.clientId());
        Console.info("WORKER COUNT: " + WorkerService.getWorkerCount());
        Console.line();
        Holder.started = true;
        return;
    }
}
