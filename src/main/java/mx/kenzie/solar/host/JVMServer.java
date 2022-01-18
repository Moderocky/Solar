package mx.kenzie.solar.host;

import mx.kenzie.solar.integration.FluidHandle;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.LocalHandle;
import mx.kenzie.solar.integration.Ownership;

import java.util.HashMap;
import java.util.Map;

abstract class JVMServer implements VMServer {
    
    protected final boolean local;
    protected final Map<Integer, Handle<?>> handles;
    
    JVMServer(boolean local) {
        this.local = local;
        this.handles = new HashMap<>();
    }
    
    @Override
    public boolean local() {
        return local;
    }
    
    @Override
    public boolean hasObject(Handle<?> handle) {
        return handles.containsValue(handle);
    }
    
    @Override
    public Ownership getOwnership(Handle<?> handle) {
        if (!handles.containsValue(handle)) return Ownership.NONE;
        if (handle instanceof LocalHandle<?>) return Ownership.FULL;
        if (handle instanceof FluidHandle<?>) return Ownership.PARTIAL;
        return Ownership.NONE;
    }
    
}
