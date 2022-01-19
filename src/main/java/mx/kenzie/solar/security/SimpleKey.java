package mx.kenzie.solar.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SimpleKey implements SecurityKey {
    
    protected static final int LENGTH = 64;
    protected final byte[] bytes;
    
    public SimpleKey(InputStream stream) throws IOException {
        this.bytes = new byte[LENGTH];
        final int length = stream.read(bytes);
        assert length == LENGTH;
    }
    
    public SimpleKey(String hash) {
        this.bytes = new byte[LENGTH];
        multiply(hash, bytes);
    }
    
    private static void multiply(String hash, byte[] bytes) {
        final int factor = 31;
        int index = 0;
        int h = 0;
        final byte[] text = hash.getBytes(StandardCharsets.US_ASCII);
        boolean complete = false;
        do for (int i = 0; i < bytes.length; i++) {
            h = factor * h + (text[index] & 0xff);
            bytes[i] ^= pack(h);
            index++;
            if (index >= text.length) {
                index = 0;
                complete = true;
            }
        } while (!complete);
    }
    
    private static byte pack(int i) {
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        final byte[] bytes = buffer.array();
        return (byte) ((bytes[0] + bytes[1] + bytes[2] + bytes[3]) / 4);
    }
    
    @Override
    public int length() {
        return LENGTH;
    }
    
    @Override
    public void write(OutputStream stream) throws IOException {
        stream.write(bytes);
    }
    
    @Override
    public boolean match(InputStream stream) throws IOException {
        final SimpleKey key = new SimpleKey(stream);
        return this.equals(key);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof SimpleKey simpleKey)) return false;
        return Arrays.equals(bytes, simpleKey.bytes);
    }
    
    @Override
    public String toString() {
        return new String(bytes);
    }
}
