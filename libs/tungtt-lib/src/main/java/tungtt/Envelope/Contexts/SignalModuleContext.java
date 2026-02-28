package tungtt.Envelope.Contexts;

public record SignalModuleContext(
    String signalKey,
    String signalContext) {

    public String jsonResponse() {
        
        return "signal";
    }
}