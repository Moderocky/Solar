package mx.kenzie.solar.integration;

import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.solar.host.VMServer;

public interface Handle<Type> {
    
    Code code();
    
    Class<Type> type();
    
    Type reference();
    
    VMServer owner();
    
    boolean isOwner(VMServer server);
    
    Ownership getOwnership(VMServer server);
    
    boolean alive();
    
    boolean hasReins();
    
    void acquireReins();
    
    Object callMethod(MethodErasure erasure, Object... arguments) throws NoSuchMethodException;
    
}
