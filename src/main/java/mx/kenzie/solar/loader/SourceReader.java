package mx.kenzie.solar.loader;

import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;

public class SourceReader {
    
    public static SourceReader reader = new SourceReader();
    
    protected boolean available = true;
    
    public byte[] source(Class<?> type) {
        try (final InputStream stream = ClassLoader.getSystemResourceAsStream(Type.getInternalName(type) + ".class")) {
            if (stream == null) return new byte[0];
            return stream.readAllBytes();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
    
    public boolean available() {
        return available;
    }
    
}
