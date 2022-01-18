package mx.kenzie.solar.connection;

public interface Protocol {
    
    byte
        OPEN = 0,
        CLOSE = 1,
        SEND_OBJECT = 2,
        RECEIVE_OBJECT = 3,
        ESTABLISH_CONNECTION = 4,
        METHOD_CALL = 5,
        REQUEST_HANDLE = 6,
        DISPATCH_HANDLE = 7;
    
}
