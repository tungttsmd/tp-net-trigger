package tungtt.Console;

import java.time.Instant;

public class Console {

    public static void info(String message) {

        if (message == null) {
            System.out.println("[INFO] empty message");
            return;
        }
        for (String line : message.split("\n")) {
            System.out.println("[INFO] " + line);
        }
    }

    public static void error(String message) {

        if (message == null) {
            System.err.println("[ERROR] empty message");
            return;
        }
        for (String line : message.split("\n")) {
            System.err.println("[ERROR] " + line);
        }
    }

    public static void warn(String message) {

        if (message == null) {
            System.err.println("[WARN] empty message");
            return;
        }
        for (String line : message.split("\n")) {
            System.err.println("[WARN] " + line);
        }
    }

    public static void success(String message) {

        if (message == null) {
            System.err.println("[SUCCESS] empty message");
            return;
        }
        for (String line : message.split("\n")) {
            System.err.println("[SUCCESS] " + line);
        }
    }

    public static void debug(String message) {

        if (message == null) {
            System.err.println("[DEBUG] empty message");
            return;
        }
        for (String line : message.split("\n")) {
            System.err.println("[DEBUG] " + line);
        }
    }

    public static void line() {

        System.out.println("\n");
    }

    public static long now() {

        return System.currentTimeMillis();
    }
    
    public static String nowISO() {
        return Instant.ofEpochMilli(System.currentTimeMillis()).toString();
    }

}
