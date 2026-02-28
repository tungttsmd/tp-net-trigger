package tungtt.Envelope.Contexts;

public record ReportModuleContext(
    String reportTitle,
    String reportHandle,
    String reportPayload) {

    public String jsonResponse() {
        
        return "report";
    }
}