package me.gking2224.buildtools.plugin;

import static org.junit.Assert.*

import org.junit.Test

class EnvironmentConfigTest {

    @Test
    public void test() {
        EnvironmentConfig ec = new EnvironmentConfig("dev")
        ec.bob("harry")
        ec.database {
            jdbc {
                url="http://localhost"
            }
            hibernate {
                connectionPoolSize=1
            }
        }
        assertEquals("harry", ec.bob)
        assertEquals(1, ec.database.hibernate.connectionPoolSize)
        assertEquals("http://localhost", ec.database.jdbc.url)
    }

    @Test
    public void testFallback() {
        EnvironmentConfig fallback = new EnvironmentConfig("all")
        fallback.missingProp("fbvalue")
        EnvironmentConfig ec = new EnvironmentConfig("dev", fallback)
        ec.database "dbvalue"
        
        assertEquals("dbvalue", ec.database)
        assertEquals("fbvalue", ec.missingProp)
    }

    @Test
    public void testFallbackDeep() {
        EnvironmentConfig fallback = new EnvironmentConfig("all")
        fallback.database {missingProp = "fbvalue"}
        EnvironmentConfig ec = new EnvironmentConfig("dev", fallback)
        ec.database {url = "dburl"}
        
        assertEquals("dburl", ec.database.url)
        assertEquals("fbvalue", ec.database.missingProp)
    }

    @Test
    public void testNonNullFallback() {
        def fbValue = "<null>"
        EnvironmentConfig fallback = new NonNullEnvironmentConfig("all", fbValue)
        EnvironmentConfig ec = new EnvironmentConfig("myconfig", fallback)
        ec.database {url = "dburl"}
        
        assertEquals(fbValue, ec.missing)
        assertEquals("dburl", ec.database.url)
        assertEquals(fbValue, ec.database.missing)
    }

}
