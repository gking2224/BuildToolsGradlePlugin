package me.gking2224.buildtools.util;

import static org.junit.Assert.*

import org.codehaus.groovy.runtime.GStringImpl
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

    @Test
    def void stringTypeTest() {
        assert !String.isAssignableFrom(GStringImpl)
    }
    
    @Test
    def void byteArrayConcatTest() {
        def b1 = [0, 1, 2, 3] 
        def b2 = [4, 5, 6, 7]
        
        assert b1.plus(b2) == [0, 1, 2, 3, 4, 5, 6, 7]
        assert b1 + b2 == [0, 1, 2, 3, 4, 5, 6, 7]
        
    }
    @Test
    def void testListLiteral() {
        def l = []
        assertNotNull(l)
    }
    
    @Test
    def void mapFind() {
        Map m = [a:{println 'boo'}]
        m.find {k->
            println "${k.value.class}"
        }
    }
}
