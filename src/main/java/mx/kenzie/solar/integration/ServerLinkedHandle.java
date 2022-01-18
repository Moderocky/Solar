package mx.kenzie.solar.integration;

import mx.kenzie.solar.host.VMServer;

public abstract class ServerLinkedHandle<Type> implements Handle<Type> {
    
    protected final VMServer server;
    
    protected ServerLinkedHandle(VMServer server) {
        this.server = server;
    }
    
    @Override
    public VMServer owner() {
        return server;
    }
    
    @Override
    public boolean isOwner(VMServer server) {
        return this.server.equals(server);
    }
}
