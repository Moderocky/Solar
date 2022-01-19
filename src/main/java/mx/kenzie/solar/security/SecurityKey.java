package mx.kenzie.solar.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SecurityKey {
    
    int length();
    
    void write(OutputStream stream) throws IOException;
    
    boolean match(InputStream stream) throws IOException;
    
}
