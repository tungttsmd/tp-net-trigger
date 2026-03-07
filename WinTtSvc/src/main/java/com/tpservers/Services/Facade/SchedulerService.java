package com.tpservers.Services.Facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import tungtt.Console.Console;

public final class SchedulerService {

    private SchedulerService() {}

    private static ScheduledExecutorService scheduler;

    public static void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "scheduler");
            t.setDaemon(true);
            return t;
        });

        long initialDelay = millisUntilNextCheck();
        scheduler.scheduleAtFixedRate(
            SchedulerService::dailyTick,
            initialDelay,
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.MILLISECONDS
        );

        Console.info("SchedulerService started — daily tick at 08:00");
    }

    private static void dailyTick() {
        int day = LocalDate.now().getDayOfMonth();

        // Mùng 1 hàng tháng: reapply wallpaper
        if (day == 1) {
            Console.info("Scheduler — day 1, reapplying wallpaper...");
            SetupService.reapply();
        }
    }

    private static long millisUntilNextCheck() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.toLocalDate().atTime(LocalTime.of(8, 0));
        if (!now.isBefore(next)) {
            next = next.plusDays(1);
        }
        return ChronoUnit.MILLIS.between(now, next);
    }
}
