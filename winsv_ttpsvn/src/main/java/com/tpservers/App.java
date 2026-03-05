package com.tpservers;

import com.tpservers.Core.EventHandler;
import com.tpservers.Services.Service;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Paths;

public class App {
    static {
        String jarDir;
        try {
            jarDir = Paths.get(
                    App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toString();
        } catch (Exception e) {
            jarDir = ".";
        }

        Dotenv dotenv = Dotenv.configure()
                .directory(jarDir)
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }

    public static void main(String[] args) {

        Service.boot();

        EventHandler.boot();
    }
}
