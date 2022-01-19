package mx.kenzie.solar.test;

import mx.kenzie.solar.connection.ConnectionOptions;
import mx.kenzie.solar.error.ConnectionError;
import mx.kenzie.solar.host.Server;
import mx.kenzie.solar.security.SecurityKey;
import mx.kenzie.solar.security.SimpleKey;
import org.junit.Test;

public class SecurityTest {
    
    @Test
    public void assumption() {
        assert new SimpleKey("hello there").equals(new SimpleKey("hello there"));
        assert !new SimpleKey("0hello there").equals(new SimpleKey("hello there"));
        assert !new SimpleKey("hello there").equals(new SimpleKey("hello there t"));
        assert !new SimpleKey("hello there").equals(new SimpleKey("hello there hello there hello there hello there"));
    }
    
    @Test
    public void simpleConnection() {
        final SecurityKey key = new SimpleKey("beef surprise");
        ConnectionError error = null;
        try (final Server local = Server.create(3032, new ConnectionOptions(key))) {
            final Server remote = Server.connect(3032);
        } catch (ConnectionError e) {
            error = e;
        }
        assert error != null;
        try (final Server local = Server.create(3032, new ConnectionOptions(key))) {
            final Server remote = Server.connect(3032, new ConnectionOptions(key));
            error = null;
        } catch (ConnectionError e) {
            error = e;
        }
        assert error == null;
    }
    
}
