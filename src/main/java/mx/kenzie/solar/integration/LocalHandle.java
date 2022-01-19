package mx.kenzie.solar.integration;

import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.mirror.MethodAccessor;
import mx.kenzie.mirror.MethodCache;
import mx.kenzie.mirror.Mirror;
import mx.kenzie.solar.host.VMServer;

public class LocalHandle<Type> extends ServerLinkedHandle<Type> implements Handle<Type> {
    
    protected final Code code;
    protected final MethodCache cache;
    protected volatile Type object;
    
    public LocalHandle(VMServer server, Code code, Type object) {
        super(server);
        this.code = code;
        this.object = object;
        this.cache = MethodCache.direct();
    }
    
    @Override
    public Code code() {
        return code;
    }
    
    @Override
    public Class<Type> type() {
        return (Class<Type>) object.getClass();
    }
    
    @Override
    public synchronized Type reference() {
        return object;
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
    public Object callMethod(MethodErasure erasure, Object... arguments) throws NoSuchMethodException {
        if (cache.has(erasure)) return cache.get(erasure).invoke(arguments);
        final MethodAccessor<?> accessor = Mirror.of(object).method(erasure.reflect());
        this.cache.cache(erasure, accessor);
        return accessor.invoke(arguments);
    }
    
    @Override
    public boolean isOwner(VMServer server) {
        return server.equals(this.server);
    }
}
