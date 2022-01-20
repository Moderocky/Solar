package mx.kenzie.solar.integration;

import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.mimic.MethodExecutor;
import mx.kenzie.mimic.Mimic;
import mx.kenzie.mirror.MethodAccessor;
import mx.kenzie.mirror.MethodCache;
import mx.kenzie.mirror.Mirror;
import mx.kenzie.solar.error.ConnectionError;
import mx.kenzie.solar.host.LocalVMServer;
import mx.kenzie.solar.host.RemoteVMServer;
import mx.kenzie.solar.host.Server;
import mx.kenzie.solar.host.VMServer;

public class FluidHandle<Type> extends BakedHandle<Type> implements Handle<Type> {
    
    protected final Class<Type> type;
    protected final Code code;
    protected final MethodCache cache;
    protected volatile Type object;
    protected volatile boolean reins;
    protected volatile VMServer server;
    protected MethodExecutor executor = this::invoke;
    
    protected FluidHandle(VMServer server, Code code, Class<Type> type) {
        this(server, code, type, null);
    }
    
    protected FluidHandle(VMServer server, Code code, Class<Type> type, MethodExecutor executor) {
        super(type);
        this.server = server;
        this.code = code;
        this.type = type;
        if (executor != null) this.executor = executor;
        this.cache = MethodCache.direct();
    }
    
    public static <Type> FluidHandle<Type> createRemote(VMServer server, Code code, Class<Type> type) {
        final FluidHandle<Type> handle = new FluidHandle<>(server, code, type);
        synchronized (handle) {
            handle.object = Mimic.create(handle.executor, type);
            handle.reins = false;
        }
        return handle;
    }
    
    @SuppressWarnings("unchecked")
    public static <Type> FluidHandle<Type> createLocal(VMServer server, Code code, Type object) {
        return (FluidHandle<Type>) createLocal(server, code, (Class<? super Type>) object.getClass(), object);
    }
    
    public static <Type> FluidHandle<Type> createLocal(VMServer server, Code code, Class<Type> type, Type object) {
        final FluidHandle<Type> handle = new FluidHandle<>(server, code, type);
        synchronized (handle) {
            handle.object = object;
            handle.reins = true;
        }
        return handle;
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
    public synchronized VMServer owner() {
        return server;
    }
    
    @Override
    public boolean isOwner(VMServer server) {
        return this.server.equals(server);
    }
    
    @Override
    public Ownership getOwnership(VMServer server) {
        if (server.equals(this.server) && hasReins()) return Ownership.FULL;
        return server.getOwnership(this);
    }
    
    @Override
    public synchronized boolean alive() {
        return object != null;
    }
    
    @Override
    public synchronized boolean hasReins() {
        return reins;
    }
    
    @Override
    public synchronized boolean acquireReins(Server server) {
        if (reins) return false;
        if (!(server instanceof LocalVMServer local))
            throw new ConnectionError("Reins must be acquired by a local server.");
        this.reins = true;
        this.object = this.server.acquire(code, local.address());
        this.server = local;
        local.install(this);
        return true;
    }
    
    @Override
    public synchronized boolean dispatchReins(Server server) {
        if (!reins) return false;
        this.reins = false;
        this.object = Mimic.create(executor, type);
        this.server = (VMServer) server;
        return true;
    }
    
    @Override
    public synchronized Object callMethod(MethodErasure erasure, Object... arguments) throws NoSuchMethodException {
        if (reins) {
            if (cache.has(erasure)) return cache.get(erasure).invoke(arguments);
            final MethodAccessor<?> accessor = Mirror.of(object).method(erasure.reflect());
            this.cache.cache(erasure, accessor);
            return accessor.invoke(arguments);
        } else return invoke(null, erasure, arguments);
    }
    
    synchronized Object invoke(Object proxy, MethodErasure erasure, Object... arguments) {
        if (this.server instanceof RemoteVMServer server) {
            return server.call(code, erasure, arguments);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "FluidHandle{" +
            "type=" + type +
            ", code=" + code +
            ", cache=" + cache +
            ", object=" + object +
            ", reins=" + reins +
            ", server=" + server +
            '}';
    }
}
