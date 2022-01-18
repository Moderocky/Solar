package mx.kenzie.solar.test;

import mx.kenzie.solar.integration.Code;
import org.junit.Test;

public class CodeTest {
    
    @Test
    public void assumption() {
        assert new Code("hello there").code() == new Code("hello there").code();
        assert new Code("hello there").code() == -3404751546271823528L;
        assert new Code("hello there beans beans thing").code() == -3404751545822886228L;
        assert new Code("bonk").code() == 13012488280416823L;
    }
    
}
