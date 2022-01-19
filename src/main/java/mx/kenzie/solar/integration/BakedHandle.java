package mx.kenzie.solar.integration;

import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.mimic.Mimic;

public abstract class BakedHandle<Type> implements Handle<Type> {
    
    protected final Type stub;
    
    public BakedHandle(Class<Type> type) {
        this.stub = Mimic.create(this::stubCall, type);
    }
    
    protected Object stubCall(Object proxy, MethodErasure erasure, Object... arguments) throws NoSuchMethodException {
        return this.callMethod(erasure, arguments);
    }
    
    @Override
    public Type stub() {
        return stub;
    }
}
