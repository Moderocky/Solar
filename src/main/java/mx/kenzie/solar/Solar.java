package mx.kenzie.solar;

import java.net.InetAddress;

public class Solar {
    
    public static Orbit connect(int port) {
        return connect(InetAddress.getLoopbackAddress(), port);
    }
    
    public static Orbit connect(InetAddress address, int port) {
        return null;
//        return new Orbit(Server.connect(address, port));
    }
    
}
