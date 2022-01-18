package mx.kenzie.solar.host;

import mx.kenzie.solar.integration.Code;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.Ownership;

public interface VMServer extends Server {
    
    static int code(Object object) {
        if (object == null) return 0;
        return object.hashCode() * object.getClass().hashCode() * 31;
    }
    
    boolean local();
    
    boolean hasObject(Handle<?> handle);
    
    Ownership getOwnership(Handle<?> handle);
    
    <Type> Handle<Type> export(Type object, Code code);
    
    <Type> Handle<Type> request(Code code);
    
}
