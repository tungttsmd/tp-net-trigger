package tungtt.Handler.CommandHandler.ShellBuilder;

import java.io.IOException;

import tungtt.Console.Console;

public class CommandShellBuilder {

    private CommandShellBuilder() {
    }

    private static class Holder {

        static final String cmd = "cmd";
        static final String executeThenExit = "/c";

        static final CommandShellBuilder INSTANCE = new CommandShellBuilder();
    }

    public static CommandShellBuilder getInstance() {
        return Holder.INSTANCE;
    }

    public static void shutdown() {

        Console.info("Shutdown Command Excuting...");
        try {
            ProcessBuilder run = new ProcessBuilder(Holder.cmd, Holder.executeThenExit, "shutdown /s /f /t 0");
            run.start();
        } catch (IOException e) {
            Console.error("Shutdown Command Failed: " + e.getMessage());
        }
    }

    public static void reset() {

        Console.info("Reset Command Excuting...");
        try {
            ProcessBuilder run = new ProcessBuilder(Holder.cmd, Holder.executeThenExit, "shutdown /r /f /t 0");
            run.start();
        } catch (IOException e) {
            Console.error("Reset Command Failed: " + e.getMessage());
        }
    }

    public static void on(Runnable runnable) {

        Console.info("Turn on Command Excuting...");
        try {
            runnable.run();
        } catch (Exception e) {
            Console.error("Turn on Command Failed: " + e.getMessage());
        }
    }


}
