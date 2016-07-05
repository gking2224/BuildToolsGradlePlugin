package me.gking2224.buildtools.util

import static org.junit.Assert.*;

import org.junit.Test;

class PropertiesAggregatorTest {

    @Test
    public void test() {
        def m = ["mapa":[a:"1", b:"2", c:"3"], "mapb":[a:"7", b:"99"]]
        
        def p = new PropertiesAggregator().aggregate(m)
        println p.hasProperty("mapa.a")
        assertTrue(p.containsKey("mapa.a"))
        assertTrue(p.containsKey("mapa"))
        assertTrue(p.containsKey("c"))
        assertEquals("1", p["mapa.a"])
        assertEquals("1", p.mapa.a)
        assertEquals("3", p.c)
     
    }

}
