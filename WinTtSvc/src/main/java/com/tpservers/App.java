package com.tpservers;

import com.tpservers.Core.EventHandler;
import com.tpservers.Services.Service;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Paths;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.ptr.IntByReference;

public class App {

     // Khu vực khai báo jna gọi API WINDOW

    interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
        int GetStdHandle(int nStdHandle);
        boolean GetConsoleMode(int h, IntByReference mode);
        boolean SetConsoleMode(int h, int mode);
    }

    // Khu vực lấy dữ liệu từ .env

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

    // Khu vực chạy lệnh thật sự
    
    public static void main(String[] args) {

        disableQuickEdit();

        Service.boot();

        EventHandler.boot();
    }

    // Khu vực gọi API WINDOW
    
    public static void disableQuickEdit() {

        try {

            int handle = Kernel32.INSTANCE.GetStdHandle(-10);
            IntByReference mode = new IntByReference();
            Kernel32.INSTANCE.GetConsoleMode(handle, mode);
            int newMode = mode.getValue();
            newMode &= ~0x0040;
            newMode &= ~0x0080;
            Kernel32.INSTANCE.SetConsoleMode(handle, newMode);
            
        } catch (Exception e) {
            // bỏ qua nếu không phải Windows
        }
    }
}
