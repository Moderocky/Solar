package mx.kenzie.solar.host;

import mx.kenzie.solar.integration.Code;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.Ownership;

import java.net.InetSocketAddress;

public interface VMServer extends Server {
    
    static int code(Object object) {
        if (object == null) return 0;
        return object.hashCode() * object.getClass().hashCode() * 31;
    }
    
    InetSocketAddress address();
    
    boolean local();
    
    boolean hasObject(Handle<?> handle);
    
    Ownership getOwnership(Handle<?> handle);
    
    <Type> Type acquire(Code code, InetSocketAddress address);
    
    <Type> Handle<Type> export(Type object, Code code);
    
    <Type> Handle<Type> request(Code code);
    
}
