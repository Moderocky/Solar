package mx.kenzie.solar.integration;

import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.mimic.MethodExecutor;
import mx.kenzie.mimic.Mimic;
import mx.kenzie.solar.host.RemoteVMServer;
import mx.kenzie.solar.host.VMServer;

public class RemoteHandle<Type> extends ServerLinkedHandle<Type> implements Handle<Type> {
    
    protected final Class<Type> type;
    protected final Code code;
    protected volatile Type object;
    protected volatile MethodExecutor executor = this::invoke;
    
    public RemoteHandle(VMServer server, Code code, Class<Type> type) {
        this(server, code, type, null);
    }
    
    protected RemoteHandle(VMServer server, Code code, Class<Type> type, MethodExecutor executor) {
        super(server);
        this.code = code;
        this.type = type;
        if (executor != null) this.executor = executor;
        this.object = Mimic.create(this.executor, type);
    }
    
    @Override
    public Code code() {
        return code;
    }
    
    @Override
    public Class<Type> type() {
        return type;
    }
    
    @Override
    public synchronized Type reference() {
        return object;
    }
    
    @Override
    public Type stub() {
        return object;
    }
    
    @Override
    public Ownership getOwnership(VMServer server) {
        if (server.equals(this.server)) return Ownership.FULL;
        return server.getOwnership(this);
    }
    
    @Override
    public boolean alive() {
        return object != null;
    }
    
    @Override
    public boolean hasReins() {
        return false;
    }
    
    @Override
    public Object callMethod(MethodErasure erasure, Object... arguments) {
        return invoke(null, erasure, arguments);
    }
    
    Object invoke(Object proxy, MethodErasure erasure, Object... arguments) {
        if (this.server instanceof RemoteVMServer server) {
            return server.call(code, erasure, arguments);
        }
        return null;
    }
}
