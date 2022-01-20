package mx.kenzie.solar.test;

import mx.kenzie.solar.host.Server;
import mx.kenzie.solar.integration.Query;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;

public class QueryTest {
    
    static InetAddress address = InetAddress.getLoopbackAddress();
    static Server local = Server.create(5459);
    static Server server = Server.connect(address, 5459);
    
    @BeforeClass
    public static void setup() {
        local.clear();
    }
    
    @AfterClass
    public static void after() {
        local.clear();
    }
    
    @Test
    public void test() {
        assert local.export(new B()) != null;
        assert local.export(new B()) != null;
        assert local.export(new C()) != null;
        assert local.query(Query.ofType(A.class)).length == 3;
        assert server.query(Query.ofType(A.class)).length == 3;
        final Query query = handle -> handle.reference() instanceof A a && a.bean();
        assert local.query(query).length == 1;
        assert server.query(query).length == 1;
    }
    
    interface A {
        
        boolean bean();
        
    }
    
    static class B implements A {
        
        @Override
        public boolean bean() {
            return false;
        }
    }
    
    static class C implements A {
        
        @Override
        public boolean bean() {
            return true;
        }
        
    }
    
    
}
