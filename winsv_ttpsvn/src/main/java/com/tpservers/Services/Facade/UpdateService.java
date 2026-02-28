package com.tpservers.Services.Facade;

import com.tpservers.Core.UpdateCore;
import tungtt.Console.Console;

public class UpdateService {
    
    public static void boot() {
        
        Console.info("Update service booting...");
        UpdateCore.update();
        Console.info("Update service booted");
    }

    public static String currentVersion() {
        Console.info("Current version service getting...");
        return UpdateCore.currentVersion();
    }

    public static String latestVersion() {
        Console.info("Latest version service getting...");
        return UpdateCore.latestVersion();
    }
}