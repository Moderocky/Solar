package mx.kenzie.solar.integration;

import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.mimic.Mimic;
import mx.kenzie.mirror.MethodAccessor;
import mx.kenzie.mirror.MethodCache;
import mx.kenzie.mirror.Mirror;
import mx.kenzie.solar.host.VMServer;

public class LocalHandle<Type> extends ServerLinkedHandle<Type> implements Handle<Type>, DestructibleHandle {
    
    protected final Code code;
    protected final MethodCache cache;
    protected final Type stub;
    protected volatile Type object;
    
    @SuppressWarnings("unchecked")
    public LocalHandle(VMServer server, Code code, Type object) {
        super(server);
        this.code = code;
        this.object = object;
        this.cache = MethodCache.direct();
        this.stub = Mimic.create(this::reference, (Class<Type>) object.getClass());
    }
    
    @Override
    public Code code() {
        return code;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Class<Type> type() {
        return (Class<Type>) object.getClass();
    }
    
    @Override
    public synchronized Type reference() {
        return object;
    }
    
    @Override
    public Type stub() {
        return stub;
    }
    
    @Override
    public Ownership getOwnership(VMServer server) {
        if (server.local()) return Ownership.FULL;
        return server.getOwnership(this);
    }
    
    @Override
    public boolean alive() {
        return object != null;
    }
    
    @Override
    public boolean hasReins() {
        return true;
    }
    
    @Override
    public synchronized Object callMethod(MethodErasure erasure, Object... arguments) throws NoSuchMethodException {
        if (object == null) return null;
        if (cache.has(erasure)) return cache.get(erasure).invoke(arguments);
        final MethodAccessor<?> accessor = Mirror.of(object).method(erasure.reflect());
        this.cache.cache(erasure, accessor);
        return accessor.invoke(arguments);
    }
    
    @Override
    public synchronized void destroy() {
        this.object = null;
    }
    
    @Override
    public boolean isOwner(VMServer server) {
        return server.equals(this.server);
    }
}
