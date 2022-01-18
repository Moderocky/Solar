package mx.kenzie.solar.marshal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Marshaller {
    
    void transfer(Object object, OutputStream stream) throws IOException;
    
    Object receive(InputStream stream) throws IOException;
    
}
