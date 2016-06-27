package me.gking2224.buildtools.util;

import static org.junit.Assert.*

import org.junit.Test

class GroovyLangTest {

    @Test
    public void testArrays() {
        assert [] instanceof java.util.Collection
        assert [0,1,2].toListString() instanceof String
        assert [0,1,2].toListString() == "[0, 1, 2]"
        
        def int[] x = [0,1,2]
        
        assertFalse("int[] is not Collection", x instanceof java.util.Collection)
        
        assertEquals("1,2", [1,2].join(","))
    }

}
