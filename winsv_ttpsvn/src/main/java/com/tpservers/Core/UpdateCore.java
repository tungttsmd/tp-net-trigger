package com.tpservers.Core;


import java.nio.file.*;
import java.io.IOException;
import com.tpservers.Services.Facade.ConfigService;
import tungtt.Console.Console;

public class UpdateCore {
    
    public static void update() {

        String updateFile = ConfigService.UPDATE_RUN_FILE();

        Path updaterDir = Paths.get("").toAbsolutePath();
        Path appRoot = updaterDir.getParent();

        if (appRoot == null) {
            Console.error("Cannot resolve app root");
            return;
        }

        Path updateFolder = appRoot.resolve(ConfigService.UPDATE_FOLDER());

        if (!Files.isDirectory(updateFolder)) {
            Console.error("Update folder not found: " + updateFolder);
            return;
        }

        try {
            new ProcessBuilder(
                    "cmd",
                    "/c",
                    updateFile
            )
            .directory(updateFolder.toFile())
            .start();

        } catch (IOException e) {
            Console.error("Update failed: " + e.getMessage());
        }
    }

    public static String currentVersion() {
        return "winsv-ttpsv-1.0.0";
    }

    public static String latestVersion() {
        return "winsv-ttpsv-1.0.4";
    }
}