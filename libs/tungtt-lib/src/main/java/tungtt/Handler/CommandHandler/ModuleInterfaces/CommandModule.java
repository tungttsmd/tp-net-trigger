package tungtt.Handler.CommandHandler.ModuleInterfaces;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;

public interface CommandModule  {

    String title();
    String command();

    void handle(CommandContext ctx) throws Exception;
}
