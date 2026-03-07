package com.tpservers.Services;

import com.tpservers.Services.Facade.HeartbeatService;
import com.tpservers.Services.Facade.MqttService;
import com.tpservers.Services.Facade.WorkerService;
import com.tpservers.Services.Facade.SetupService;
import com.tpservers.Services.Facade.SchedulerService;

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

        Console.info("[DEV Note] The methods for handling service faulty restarts and retries are located in the error handling section of each Facade Service (source code)");
        Console.info("[DEV Note] Please do not spaghetti developing the methods for restarting and retrying faulty services to any location other than the Facade Service (source code)");
        Console.line();

        /* ========== MQTT ============ */
        Console.info("[1/3] Starting MQTT Service...");
        MqttService.start();
        Console.info("[1/3] MQTT Service — OK");
        Console.line();

        /* ========== WORKER POOL ============ */
        Console.info("[2/3] Starting Worker Pool...");
        WorkerService.start();
        Console.info("[2/3] Worker Pool — OK  (workers: " + WorkerService.getWorkerCount() + ")");
        Console.line();

        /* ========== SET UP DEVICE ============ */
        Console.info("[3/3] Setting up device...");
        try {
            // Dịch vụ này chỉ thực hiện một lần và có thể bỏ qua nếu fail, không được exit
            SetupService.boot();
            Console.info("[3/3] Setup Service — OK");
        } catch (Exception e) {
            Console.error("[3/3] Setup Service — FAILED: " + e.getMessage());
        }
        Console.line();

        /* ========== SCHEDULER ============ */
        SchedulerService.start();

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
