package mx.kenzie.solar.marshal;

import mx.kenzie.jupiter.stream.OutputStreamController;
import mx.kenzie.jupiter.stream.Stream;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BinaryMarshaller implements Marshaller {
    
    @Override
    public void transfer(Object object, OutputStream target) throws IOException {
        this.writeClass(object.getClass(), target);
        this.deconstruct(object, target);
    }
    
    @Override
    public Object receive(InputStream stream) throws IOException {
        return null;
    }
    
    public void writeClass(Class<?> type, OutputStream target) throws IOException {
        try (final ObjectOutputStream stream = new ObjectOutputStream(Stream.keepalive(target))) {
            stream.writeObject(type);
        }
    }
    
    public void deconstruct(Object object, OutputStream target) throws IOException {
        final OutputStreamController stream = Stream.controller(target);
        final Unsafe unsafe = InternalAccess.unsafe();
        Class<?> cls = object.getClass();
        do {
            for (final Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                long offset = unsafe.objectFieldOffset(field);
                if (field.getType() == long.class) {
                    stream.writeLong(unsafe.getLong(object, offset));
                } else if (field.getType() == int.class) {
                    stream.writeInt(unsafe.getInt(object, offset));
                } else if (field.getType() == short.class) {
                    stream.writeShort(unsafe.getShort(object, offset));
                } else if (field.getType() == byte.class) {
                    stream.write(unsafe.getByte(object, offset));
                } else if (field.getType() == boolean.class) {
                    stream.write(unsafe.getBoolean(object, offset) ? 1 : 0);
                } else if (field.getType() == char.class) {
                    stream.writeChar(unsafe.getChar(object, offset));
                } else if (field.getType() == float.class) {
                    stream.writeFloat(unsafe.getFloat(object, offset));
                } else if (field.getType() == double.class) {
                    stream.writeDouble(unsafe.getDouble(object, offset));
                } else {
                    final Object next = unsafe.getObject(object, offset);
                    this.deconstruct(next, target);
                }
            }
        } while ((cls = cls.getSuperclass()) != null);
    }
    
}
