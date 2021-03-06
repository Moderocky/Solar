package mx.kenzie.solar.connection;

public interface Protocol {
    
    byte
        OPEN = 0,
        CLOSE = 1,
        SEND_OBJECT = 2, // code, object, mode
        RECEIVE_OBJECT = 3, // code, address -> object
        ESTABLISH_CONNECTION = 4,
        METHOD_CALL = 5,
        REQUEST_HANDLE = 6,
        DISPATCH_HANDLE = 7, // code, owner, object
        EMPTY = 8,
        TRANSFER_CLASS = 9, // name, length, bytes
        QUERY = 10, // query
        FAIL_CONNECTION = 11,
        DESTROY_HANDLE = 12,
        HAS_HANDLE = 13, // code -> answer
        GET_CONTENTS = 14; // -> codes
    
}
