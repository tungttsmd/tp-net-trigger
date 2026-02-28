package tungtt.Envelope.Contexts;

public record HeartbeatModuleContext(
    String heartbeatType,
    String heartbeatNote
) {
    
    public String jsonResponse() {
        
        return "heartbeat";
    }
}