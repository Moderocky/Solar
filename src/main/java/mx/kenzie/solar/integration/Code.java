package mx.kenzie.solar.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.constant.Constable;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public record Code(long code) implements Serializable {
    
    public Code(String code) {
        this(convert(code));
    }
    
    public static long convert(String string) {
        if (string.length() < 5) return (long) string.hashCode() << 32L | Objects.hash(string.toLowerCase()) * 31L;
        if (string.length() < 9)
            return (long) string.substring(0, 4).hashCode() << 32L | (long) string.substring(4).hashCode();
        return (long) string.substring(0, 8).hashCode() << 32L | (long) string.substring(8).hashCode();
    }
    
    public Code(Object object) {
        this(convert(object));
    }
    
    private static long convert(Object object) {
        if (object instanceof String string) return convert(string);
        if (object instanceof Constable || object instanceof Record) { // likely to not want an identity confirmation check
            return (long) object.hashCode() << 32 | object.hashCode() * 31L;
        } else {
            return (long) object.hashCode() << 32 | System.identityHashCode(object);
        }
    }
    
    public Code() {
        this(ThreadLocalRandom.current().nextLong());
    }
    
    public Code(byte[] bytes) {
        this(convert(bytes));
    }
    
    public static long convert(byte[] bytes) {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(0, bytes);
        return buffer.getLong(0);
    }
    
    public static Code read(InputStream stream) throws IOException {
        final byte[] bytes = new byte[8];
        stream.read(bytes);
        return new Code(bytes);
    }
    
    public byte[] bytes() {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(code);
        return buffer.array();
    }
    
    @Override
    public String toString() {
        return "" + code;
    }
}
