package mx.kenzie.solar.host;

import mx.kenzie.solar.connection.ConnectionOptions;
import mx.kenzie.solar.error.ConnectionError;
import mx.kenzie.solar.integration.Code;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.HandlerMode;
import mx.kenzie.solar.marshal.Marshaller;
import mx.kenzie.solar.security.SecurityKey;
import mx.kenzie.solar.security.SimpleKey;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public interface Server extends AutoCloseable {
    
    SecurityKey OPEN_KEY = new SimpleKey("jasmine");
    
    static Server connect(InetSocketAddress address) {
        return connect(address.getAddress(), address.getPort());
    }
    
    static Server connect(InetAddress address, int port) throws ConnectionError {
        return connect(address, port, ConnectionOptions.DEFAULT);
    }
    
    static Server connect(InetAddress address, int port, ConnectionOptions options) throws ConnectionError {
        return RemoteVMServer.connect(address, port, options);
    }
    
    static Server connect(ConnectionOptions options) throws ConnectionError {
        return connect(LocalVMServer.defaultPort(), options);
    }
    
    static Server connect(int port, ConnectionOptions options) throws ConnectionError {
        return connect(InetAddress.getLoopbackAddress(), port, options);
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
    
    static Server create(ConnectionOptions options) {
        return create(LocalVMServer.defaultPort(), options);
    }
    
    static Server create(int port, ConnectionOptions options) {
        return LocalVMServer.create(port, options);
    }
    
    <Type> Handle<Type> export(Type object, Code code, HandlerMode mode);
    
    <Type> Handle<Type> export(Type object, Code code);
    
    <Type> void export(Handle<Type> handle);
    
    <Type> Handle<Type> request(Code code);
    
    void clear();
    
    default boolean has(Handle<?> handle) {
        return this.has(handle.code());
    }
    
    boolean has(Code code);
    
    default void remove(Handle<?> handle) {
        this.remove(handle.code());
    }
    
    void remove(Code code);
    
    void close();
    
    Code[] contents();
    
    void marshal(Marshaller marshaller);
    
}
