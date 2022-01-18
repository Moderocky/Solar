package mx.kenzie.solar.host;

import mx.kenzie.solar.error.ConnectionError;
import mx.kenzie.solar.integration.Code;
import mx.kenzie.solar.integration.Handle;

import java.net.InetAddress;

public interface Server {
    
    <Type> Handle<Type> export(Type object, Code code);
    
    <Type> Handle<Type> request(Code code);
    
    static VMServer connect(InetAddress address, int port) throws ConnectionError {
        return RemoteVMServer.connect(address, port);
    }
    
    static VMServer connect(int port) throws ConnectionError {
        return RemoteVMServer.connect(InetAddress.getLoopbackAddress(), port);
    }
    
    static VMServer create(int port) {
        return LocalVMServer.create(port);
    }
    
}
