package tungtt.Envelope.Contexts;

public record CommandModuleContext(
    String commandTitle,
    String command,
    String commandContext) {

    public String jsonResponse() {

        return "command";
    }
}