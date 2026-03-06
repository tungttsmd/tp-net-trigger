package com.tpservers.Services;

import com.tpservers.Services.Facade.HeartbeatService;
import com.tpservers.Services.Facade.MqttService;
import com.tpservers.Services.Facade.WorkerService;
import com.tpservers.Services.Facade.SetupService;

import com.tpservers.Repositories.MetaRespository;
import com.tpservers.Services.Facade.ConfigService;
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

        if (Holder.started) {
            Console.error("Services already started");
            return;
        }

        Console.info("════════════════════════════════════════");
        Console.info("  tp-net-trigger  |  booting...");
        Console.info("════════════════════════════════════════");
        Console.line();

        /* ========== MQTT ============ */
        Console.info("[1/3] Starting MQTT Service...");
        try {
            MqttService.start();
            Console.info("[1/3] MQTT Service — OK");
        } catch (Exception e) {
            Console.error("[1/3] MQTT Service — FAILED: " + e.getMessage());
        }
        Console.line();

        /* ========== WORKER POOL ============ */
        Console.info("[2/3] Starting Worker Pool...");
        try {
            WorkerService.start();
            Console.info("[2/3] Worker Pool — OK  (workers: " + WorkerService.getWorkerCount() + ")");
        } catch (Exception e) {
            Console.error("[2/3] Worker Pool — FAILED: " + e.getMessage());
        }
        Console.line();

        /* ========== SET UP DEVICE ============ */
        Console.info("[3/3] Setting up device...");
        try {
            SetupService.boot();
            Console.info("[3/3] Setup Service — OK");
        } catch (Exception e) {
            Console.error("[3/3] Setup Service — FAILED: " + e.getMessage());
        }
        Console.line();

        /* ========== HEARTBEAT ============ */
        HeartbeatService.start(12);

        /* ========== SUMMARY ============ */
        Console.info("════════════════════════════════════════");
        Console.info("  HOST FROM LOCAL IP      : " + MetaRespository.hostLocalIp());
        Console.info("  HOST ID                 : " + ConfigService.HOST_ID());
        Console.info("  HOST IDENT              : " + MqttService.clientId());
        Console.info("  THREADS                 : " + WorkerService.getWorkerCount());
        Console.info("════════════════════════════════════════");
        Console.line();

        Holder.started = true;
    }
}
