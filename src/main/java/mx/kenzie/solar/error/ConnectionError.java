package mx.kenzie.solar.error;

public class ConnectionError extends IOError {
    
    public ConnectionError() {
        super();
    }
    
    public ConnectionError(String message) {
        super(message);
    }
    
    public ConnectionError(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConnectionError(Throwable cause) {
        super(cause);
    }
    
    protected ConnectionError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
