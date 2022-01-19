package mx.kenzie.solar.test;

import mx.kenzie.solar.host.Server;
import mx.kenzie.solar.integration.Code;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.HandlerMode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Serializable;
import java.net.InetAddress;

public class FluidTest {
    
    static InetAddress address = InetAddress.getLoopbackAddress();
    static Server local = Server.create(5457);
    static Server other = Server.create(5458);
    static Server server = Server.connect(address, 5457);
    
    @BeforeClass
    public static void setup() {
    }
    
    @AfterClass
    public static void after() {
        local.close();
        server.close();
    }
    
    @Test
    @SuppressWarnings("all")
    public void test() {
        final Code code = new Code("hello there");
        final Handle<Blob> original = local.export(new Blob(), code, HandlerMode.FLUID);
        final Handle<Blob> handle = server.request(code);
        final Blob first = original.reference();
        final Blob remote = handle.reference();
        assert remote != first;
        assert remote.toString().equals("hello");
        handle.acquireReins(other);
        assert handle.hasReins();
        assert !original.hasReins();
        assert handle.reference() instanceof Blob;
        assert original.reference() instanceof Blob;
        assert handle.reference().getClass() == Blob.class;
        assert original.reference().getClass() != Blob.class;
        assert handle.reference().toString().equals("hello");
        assert original.reference().toString().equals("hello");
    }
    
    @Test
    @SuppressWarnings("all")
    public void exportRemote() {
        final Code code = new Code("bean potato cheese");
        final Handle<Blob> handle = server.export(new Blob(), code, HandlerMode.FLUID);
        assert !handle.hasReins();
        assert handle.reference() instanceof Blob;
        assert handle.reference().getClass() != Blob.class;
        assert handle.reference().toString().equals("hello");
    }
    
    static class Blob implements Serializable {
        
        @Override
        public String toString() {
            return "hello";
        }
        
    }
}
