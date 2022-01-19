package mx.kenzie.solar.host;

import mx.kenzie.solar.error.ConnectionError;
import mx.kenzie.solar.integration.Code;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.HandlerMode;
import mx.kenzie.solar.marshal.Marshaller;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public interface Server extends AutoCloseable {
    
    static Server connect(InetSocketAddress address) {
        return connect(address.getAddress(), address.getPort());
    }
    
    static Server connect(InetAddress address, int port) throws ConnectionError {
        return RemoteVMServer.connect(address, port);
    }
    
    static Server connect() throws ConnectionError {
        return connect(LocalVMServer.defaultPort());
    }
    
    static Server connect(int port) throws ConnectionError {
        return connect(InetAddress.getLoopbackAddress(), port);
    }
    
    static Server create() {
        return create(LocalVMServer.defaultPort());
    }
    
    static Server create(int port) {
        return LocalVMServer.create(port);
    }
    
    <Type> Handle<Type> export(Type object, Code code, HandlerMode mode);
    
    <Type> Handle<Type> export(Type object, Code code);
    
    <Type> void export(Handle<Type> handle);
    
    <Type> Handle<Type> request(Code code);
    
    void close();
    
    void marshal(Marshaller marshaller);
    
}
