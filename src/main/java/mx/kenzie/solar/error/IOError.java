package mx.kenzie.solar.error;

public class IOError extends Error {
    
    public IOError() {
        super();
    }
    
    public IOError(String message) {
        super(message);
    }
    
    public IOError(String message, Throwable cause) {
        super(message, cause);
    }
    
    public IOError(Throwable cause) {
        super(cause);
    }
    
    protected IOError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
