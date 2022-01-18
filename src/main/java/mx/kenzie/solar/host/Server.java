package mx.kenzie.solar.host;

import mx.kenzie.solar.error.ConnectionError;

import java.net.InetAddress;

public interface Server {
    
    static VMServer connect(InetAddress address, int port) throws ConnectionError {
        return RemoteVMServer.connect(address, port);
    }
    
    static VMServer create(int port) {
        return LocalVMServer.create(port);
    }
    
}
