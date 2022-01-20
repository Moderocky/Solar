package mx.kenzie.solar.test;

import mx.kenzie.solar.host.Server;
import mx.kenzie.solar.integration.Code;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;

public class TransferTest {
    
    static InetAddress address = InetAddress.getLoopbackAddress();
    static Server local = Server.create(5456);
    static Server server = Server.connect(address, 5456);
    
    @BeforeClass
    public static void setup() {
        final Code code = new Code("thing");
        final MyCoolThing thing = new MyCoolThing();
        assert local.export(thing, code) != null;
    }
    
    @Test
    public void export() {
        final Code code = new Code("bean");
        final MyCoolThing thing = new MyCoolThing();
        assert local.export(thing, code) != null;
    }
    
    @Test
    public void retrieve() {
        final Code code = new Code("thing");
        final MyCoolThing remote = server.<MyCoolThing>request(code).reference();
        assert remote != null;
        assert remote.toString().equals("hello from the other side");
    }
    
    @Test
    @SuppressWarnings("all")
    public void test() {
        final Code code = new Code("hello there");
        final MyCoolThing thing = new MyCoolThing();
        assert local.export(thing, code) != null;
        final MyCoolThing remote = server.<MyCoolThing>request(code).reference();
        assert remote != thing;
        assert remote != null;
        assert remote.toString().equals("hello from the other side");
        assert remote.getLength("bean") == 4;
        assert remote instanceof MyCoolThing;
        assert local.has(code);
        assert server.has(code);
        final Code check = new Code("lettuce");
        assert local.export(new MyCoolThing(), check) != null;
        assert local.has(check);
        local.remove(check);
        assert !local.has(check);
    }
    
    static class MyCoolThing {
        
        public int getLength(String string) {
            return string.length();
        }
        
        @Override
        public String toString() {
            return "hello from the other side";
        }
        
    }
    
}
