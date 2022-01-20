package mx.kenzie.solar.integration;

import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.solar.host.Server;
import mx.kenzie.solar.host.VMServer;

public interface Handle<Type> {
    
    Code code();
    
    Class<Type> type();
    
    Type reference();
    
    Type stub();
    
    VMServer owner();
    
    boolean isOwner(VMServer server);
    
    Ownership getOwnership(VMServer server);
    
    boolean alive();
    
    boolean hasReins();
    
    default boolean acquireReins(Server server) {
        return false;
    }
    
    default boolean dispatchReins(Server server) {
        return false;
    }
    
    Object callMethod(MethodErasure erasure, Object... arguments) throws NoSuchMethodException;
    
}
