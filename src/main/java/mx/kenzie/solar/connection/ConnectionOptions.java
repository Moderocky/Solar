package mx.kenzie.solar.connection;

import mx.kenzie.solar.host.Server;
import mx.kenzie.solar.security.SecurityKey;

public record ConnectionOptions(SecurityKey key, boolean ssl) {
    
    public static final ConnectionOptions DEFAULT = new ConnectionOptions(false);
    
    public ConnectionOptions(boolean ssl) {
        this(Server.OPEN_KEY, ssl);
    }
    
    public ConnectionOptions(SecurityKey key) {
        this(key, false);
    }
    
}
