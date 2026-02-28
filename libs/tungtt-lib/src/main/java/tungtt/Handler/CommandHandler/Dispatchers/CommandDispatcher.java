package tungtt.Handler.CommandHandler.Dispatchers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import tungtt.Handler.CommandHandler.CommandRegistry.CommandRegistry;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;
import tungtt.Handler.CommandHandler.Configs.CommandConfig;

import tungtt.Envelope.Contexts.EnvelopeMetaContext;
import tungtt.Envelope.Contexts.EnvelopeSecureContext;

import tungtt.Console.Console;

public final class CommandDispatcher {
    
    private final CommandConfig config;

    public CommandDispatcher(CommandConfig config) {

        this.config = config;
    }
    
    public static CommandDispatcher getInstance(CommandConfig config) {
        return new CommandDispatcher(config);
    }

    public void handle(String message) {

        JsonObject root = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = root.getAsJsonObject("data");

        String title = getString(data, "command_title");
        String command = getString(data, "command");

        String hostId = String.valueOf(this.config.hostId());

        EnvelopeMetaContext metaContext = new EnvelopeMetaContext(
                hostId,
                this.config.hostFromPrefix() + "-" + hostId,
                this.config.hostVersion(),
                Console.now());

        EnvelopeSecureContext secureContext = new EnvelopeSecureContext(
            "[coming soon]",
            "[coming soon]");

        CommandContext ctx = new CommandContext(
                hostId,
                metaContext,
                secureContext,
                root,
                data
        );

        CommandModule module = CommandRegistry.resolve(title, command);
CommandRegistry.view();
        if (module == null) {
            Console.error("The module is an invalidated module");
            return;
        }

        try {
            module.handle(ctx);
        } catch (Exception e) {
            Console.error("A internal Command Dispatcher handle error");
        }
    }
    
    private static String getString(JsonObject obj, String key) {

        if (obj == null || key == null) {
            return null;
        }
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsString();
    }
}
