package mx.kenzie.solar.integration;

import java.io.Serializable;

@FunctionalInterface
public interface Query extends Serializable {
    
    static Query ofType(Class<?> type) {
        return new TypeQuery(type);
    }
    
    default boolean noMatch(Handle<?> handle) {
        return !this.matches(handle);
    }
    
    boolean matches(Handle<?> handle);
    
    record TypeQuery(Class<?> type) implements Query {
        
        @Override
        public boolean matches(Handle<?> handle) {
            return type.isAssignableFrom(handle.type());
        }
        
    }
}

