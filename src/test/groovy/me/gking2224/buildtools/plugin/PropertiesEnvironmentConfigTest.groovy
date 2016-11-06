package me.gking2224.buildtools.plugin

import static org.junit.Assert.*

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test


class PropertiesEnvironmentConfigTest {
 
    def project
    
    @Before
    void before() {
        project = ProjectBuilder.builder().build()
        
        project.ext.env="unit"
    }
    @Test
    public void test() {
        
        Properties p = new Properties()
        p.put("name", "Donald")
        p.put("intro", "my name is \${name}")
        p.put("salutation", "Hello, \${intro}")
        
        def plist = []
        plist << p
        PropertiesEnvironmentConfig pec = new PropertiesEnvironmentConfig("unit", project, plist)
        
        assertEquals("Hello, my name is Donald", pec.salutation)
    }
    @Test
    public void testMultiple() {
        
        Properties p = new Properties()
        p.put("greeting", "\${myvalue}")
        p.put("myvalue", "\${hello}, \${world}")
        p.put("hello", "Hello")
        p.put("world", "World")
        
        def plist = []
        plist << p
        PropertiesEnvironmentConfig pec = new PropertiesEnvironmentConfig("unit", project, plist)
        
        assertEquals("Hello, World", pec.greeting)
    }
    @Test
    public void testDefault() {
        
        Properties p = new Properties()
        p.put("greeting", "\${myvalue}")
        p.put("myvalue", "\${hello:Hello}, \${world}")
        p.put("world", "World")
        
        def plist = []
        plist << p
        PropertiesEnvironmentConfig pec = new PropertiesEnvironmentConfig("unit", project, plist)
        
        assertEquals("Hello, World", pec.greeting)
    }
}
