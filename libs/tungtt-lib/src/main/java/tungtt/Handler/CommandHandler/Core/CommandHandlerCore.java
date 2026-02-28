package tungtt.Handler.CommandHandler.Core;

import tungtt.Handler.CommandHandler.Configs.CommandConfig;
import tungtt.Handler.CommandHandler.ModuleInterfaces.CommandModule;
import tungtt.Handler.CommandHandler.CommandRegistry.CommandRegistry;
import tungtt.Handler.CommandHandler.CommandAnnotation.ModuleAnnotation;

import org.reflections.Reflections;


public final class CommandHandlerCore {

    public static void boot(CommandConfig config) {

        Reflections ref = new Reflections(
            config.moduleStoreCommandHandlerPackageName()
        );

        for (Class<?> cls : ref.getTypesAnnotatedWith(ModuleAnnotation.class)) {
            try {
                CommandRegistry.register(
                    (CommandModule) cls.getDeclaredConstructor().newInstance()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
