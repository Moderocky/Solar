package mx.kenzie.solar;

import mx.kenzie.jupiter.socket.SocketHub;

import java.net.InetAddress;

public class Orbit {
    
    protected final SocketHub hub;
    
    Orbit(SocketHub hub) {
        this.hub = hub;
    }
    
    public void connect(InetAddress address, int port) {
    
    }
    
}
