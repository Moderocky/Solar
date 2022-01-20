package mx.kenzie.solar.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public interface SecurityKey extends Serializable {
    
    int length();
    
    void write(OutputStream stream) throws IOException;
    
    boolean match(InputStream stream) throws IOException;
    
}
