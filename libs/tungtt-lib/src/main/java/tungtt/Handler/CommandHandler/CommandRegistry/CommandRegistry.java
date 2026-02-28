package tungtt.Handler.CommandHandler.CommandRegistry;

import tungtt.Console.Console;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;

import java.util.HashMap;
import java.util.Map;

public final class CommandRegistry {

    private static final Map<String, CommandModule> REGISTRY = new HashMap<>();

    private CommandRegistry() {}

    public final class Holder {
        final static CommandRegistry INSTANCE = new CommandRegistry();
    }

    public static CommandRegistry getInstance() {
        return Holder.INSTANCE;
    }

    /* ========================= BOOTSTRAP REGISTER NEEDED! ========================= */

    public static void register(CommandModule module) {

        String key = key(module.title(), module.command());
        REGISTRY.put(key, module);
    }

    public static void view() {
        Console.info("Command Registry:");
        for (String key : REGISTRY.keySet()) {
            Console.info(key);
        }
    }

    public static CommandModule resolve(String commandTitle, String command) {

        return REGISTRY.get(key(commandTitle, command));
    }

    private static String key(String commandTitle, String command) {

        return commandTitle + ":" + command;
    }
}
