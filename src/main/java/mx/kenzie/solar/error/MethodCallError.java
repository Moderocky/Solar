package mx.kenzie.solar.error;

public class MethodCallError extends IOError {
    
    public MethodCallError() {
        super();
    }
    
    public MethodCallError(String message) {
        super(message);
    }
    
    public MethodCallError(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MethodCallError(Throwable cause) {
        super(cause);
    }
    
    protected MethodCallError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
