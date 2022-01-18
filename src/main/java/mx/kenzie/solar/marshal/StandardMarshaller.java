package mx.kenzie.solar.marshal;

import mx.kenzie.jupiter.stream.Stream;

import java.io.*;

public class StandardMarshaller implements Marshaller {
    
    @Override
    public void transfer(Object object, OutputStream target) throws IOException {
        final OutputStream output = Stream.keepalive(target);
        try (final ObjectOutputStream transfer = new ObjectOutputStream(output)) {
            transfer.writeObject(object);
        }
    }
    
    @Override
    public Object receive(InputStream target) throws IOException {
        final InputStream input = Stream.keepalive(target);
        try (final ObjectInputStream transfer = new ObjectInputStream(input)) {
            return transfer.readObject();
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
    
}
