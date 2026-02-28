package com.tpservers;

import com.tpservers.Core.EventHandler;
import com.tpservers.Services.Service;

import com.tpservers.Services.Facade.UpdateService;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Path;

public class App {
    static {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }

    public static void main(String[] args) {

        // Start: Run update service
        // UpdateService.boot(); Khoá tạm thời để manual test update
        // End: Run update service

        // Start: Run service
        Service.boot();
        EventHandler.boot();
        // End: Run service
    }
}
