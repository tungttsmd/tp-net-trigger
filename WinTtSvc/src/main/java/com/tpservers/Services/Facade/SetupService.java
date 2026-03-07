package com.tpservers.Services.Facade;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import tungtt.Console.Console;
import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public final class SetupService {

    interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);
        int SystemParametersInfoA(int uAction, int uParam, String lpvParam, int fuWinIni);
    }

    private SetupService() {}

    public static void boot() {
        try {
            applyWallpaper(ConfigService.WALLPAPER_URL());
        } catch (Exception e) {
            Console.error("Failed to apply wallpaper: " + e.getMessage());
        }
    }

    public static void reapply() {
        try {
            String jarDir = Paths.get(
                SetupService.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).getParent().toString();
            Path wallpaperFile = Paths.get(jarDir + "\\wallpaper\\wallpaper.jpg");
            Files.deleteIfExists(wallpaperFile);
            applyWallpaper(ConfigService.WALLPAPER_URL());
        } catch (Exception e) {
            Console.error("Failed to reapply wallpaper: " + e.getMessage());
        }
    }

    private static void applyWallpaper(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        String jarDir = Paths.get(
            SetupService.class.getProtectionDomain().getCodeSource().getLocation().toURI()
        ).getParent().toString();
        String wallpaperDir = jarDir + "\\wallpaper";
        Files.createDirectories(Paths.get(wallpaperDir));
        String fullPath = wallpaperDir + "\\wallpaper.jpg";

        // Tải ảnh với timeout 15 giây
        URL url = new URL(imageUrl);
        java.net.URLConnection conn = url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
        }

        User32.INSTANCE.SystemParametersInfoA(20, 0, fullPath, 3);
        Console.info("Wallpaper applied: " + fullPath);
    }
}