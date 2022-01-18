package mx.kenzie.solar;

import mx.kenzie.solar.host.Server;

import java.net.InetAddress;

public class Solar {
    
    public static Orbit connect(int port) {
        return connect(InetAddress.getLoopbackAddress(), port);
    }
    
    public static Orbit connect(InetAddress address, int port) {
        return new Orbit(Server.connect(address, port));
    }
    
}
