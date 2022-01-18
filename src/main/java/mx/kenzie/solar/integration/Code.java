package mx.kenzie.solar.integration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public record Code(long code) {
    
    public Code(String code) {
        this(convert(code));
    }
    
    public static long convert(String string) {
        if (string.length() < 9) return string.hashCode();
        return string.substring(8).hashCode() * 31L | (long) string.substring(8).hashCode() << 4L;
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
    
}
