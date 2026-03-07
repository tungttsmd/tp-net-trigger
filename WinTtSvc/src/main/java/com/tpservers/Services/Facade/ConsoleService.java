package com.tpservers.Services.Facade;

public class ConsoleService {
    private ConsoleService() {
    }

    private static class Holder {
        final static ConsoleService INSTANCE = new ConsoleService();
    }

    public static ConsoleService getInstance() {
        return Holder.INSTANCE;
    }

    public static void info(String message) {
        for (String line : message.split("\n")) {
            System.out.println("[INFO] " + line);
        }
    }

    public static void success(String message) {
        for (String line : message.split("\n")) {
            System.out.println("[SUCCESS] " + line);
        }
    }

    public static void error(String message) {
        for (String line : message.split("\n")) {
            System.err.println("[ERROR] " + line);
        }
    }

    public static void debug(String message) {
        for (String line : message.split("\n")) {
            System.out.println("[DEBUG] " + line);
        }
    }

    public static void breakLine() {
        System.out.println("\n");
    }
}
