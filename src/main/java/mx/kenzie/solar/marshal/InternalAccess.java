package mx.kenzie.solar.marshal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.PrivilegedExceptionAction;

@SuppressWarnings("removal")
public class InternalAccess {
    static Unsafe unsafe;
    
    static {
        try {
            unsafe = java.security.AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
                final Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                return (Unsafe) field.get(null);
            });
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
    
    static Unsafe unsafe() {
        return unsafe;
    }
}
