package me.gking2224.buildtools.util

import static org.junit.Assert.*;

import org.junit.Test;

class PropertiesAggregatorTest {

    @Test
    public void test() {
        def m = ["mapa":[a:"1", b:"2"], "mapb":[a:"7", b:"99"]]
        
        
        assertEquals(
             ["mapa.a":"1", "mapa.b":"2", "mapb.a":"7", "mapb.b":"99"],
             new PropertiesAggregator().aggregate(m))
    }

}
