package tungtt.HardwareProfile.Exceptions;

public class HardwareProfileException extends RuntimeException {
    
    public HardwareProfileException(String message) {
        super(message);
    }
    public HardwareProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}
