package mx.kenzie.solar.marshal;

import mx.kenzie.jupiter.stream.InputStreamController;
import mx.kenzie.jupiter.stream.OutputStreamController;
import mx.kenzie.jupiter.stream.Stream;
import mx.kenzie.solar.error.ConnectionError;
import sun.misc.Unsafe;

import java.io.*;
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
        try {
            final Class<?> type = this.readClass(stream);
            return construct(type, stream);
        } catch (InstantiationException e) {
            throw new ConnectionError(e);
        }
    }
    
    public void writeClass(Class<?> type, OutputStream target) throws IOException {
        try (final ObjectOutputStream stream = new ObjectOutputStream(Stream.keepalive(target))) {
            stream.writeObject(type);
        }
    }
    
    public Class<?> readClass(InputStream target) throws IOException {
        try (final ObjectInputStream stream = new ObjectInputStream(Stream.keepalive(target))) {
            return (Class<?>) stream.readObject();
        } catch (ClassNotFoundException ex) {
            throw new NoClassDefFoundError(ex.getMessage());
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
                final long offset = unsafe.objectFieldOffset(field);
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
    
    public Object construct(Class<?> cls, InputStream target) throws InstantiationException, IOException {
        final InputStreamController stream = Stream.controller(target);
        final Unsafe unsafe = InternalAccess.unsafe();
        final Object object = unsafe.allocateInstance(cls);
        do {
            for (final Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                final long offset = unsafe.objectFieldOffset(field);
                if (field.getType() == long.class) {
                    unsafe.putLong(object, offset, stream.readLong());
                } else if (field.getType() == int.class) {
                    unsafe.putInt(object, offset, stream.readInt());
                } else if (field.getType() == short.class) {
                    unsafe.putShort(object, offset, stream.readShort());
                } else if (field.getType() == byte.class) {
                    unsafe.putByte(object, offset, (byte) stream.read());
                } else if (field.getType() == boolean.class) {
                    unsafe.putBoolean(object, offset, stream.read() == 1);
                } else if (field.getType() == char.class) {
                    unsafe.putChar(object, offset, stream.readChar());
                } else if (field.getType() == float.class) {
                    unsafe.putFloat(object, offset, stream.readFloat());
                } else if (field.getType() == double.class) {
                    unsafe.putDouble(object, offset, stream.readDouble());
                } else {
                    this.construct(field.getType(), target);
                }
            }
        } while ((cls = cls.getSuperclass()) != null);
        return object;
    }
    
}
