package mx.kenzie.solar.host;

import mx.kenzie.solar.integration.FluidHandle;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.LocalHandle;
import mx.kenzie.solar.integration.Ownership;
import mx.kenzie.solar.marshal.Marshaller;
import mx.kenzie.solar.marshal.StandardMarshaller;

import java.util.HashMap;
import java.util.Map;

abstract class JVMServer implements VMServer {
    
    protected final boolean local;
    protected final Map<Integer, Handle<?>> handles;
    protected Marshaller marshaller;
    
    JVMServer(boolean local) {
        this.local = local;
        this.handles = new HashMap<>();
        this.marshaller = new StandardMarshaller();
    }
    
    @Override
    public void marshal(Marshaller marshaller) {
        this.marshaller = marshaller;
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
