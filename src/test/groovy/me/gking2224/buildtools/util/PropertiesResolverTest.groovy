package me.gking2224.buildtools.util

import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class PropertiesResolverTest {

    def Properties p = new Properties()
    def Project project
    
    @Before
    void before() {
        project = ProjectBuilder.builder().build()
        p.put("a.b.c", "x")
        p.put("a.b.d", "y")
        p.put("x", "z")
        p.put("systemProp.abc", "def")
        p.put("systemProp.my.prop", "value")
        p.put("systemProp.my.setting", "value2")
    }
    
    @Test
    public void test() {
        def pr = new PropertiesResolver(project)
        pr.resolveProperties(p)
        
        assertTrue(project.hasProperty("a"))
//        assertTrue(project.hasProperty("a.b"))
        assertTrue(project.hasProperty("a.b.c"))
        assertTrue(project.hasProperty("a.b.d"))
        assertTrue(project.hasProperty("x"))
        assertTrue(project.a instanceof Map)
        assertEquals("x", project.a.b.c)
        assertEquals("y", project.a.b.d)
        assertEquals("z", project.x)
        
        assertTrue(!project.hasProperty("abc"))
        assertTrue(!project.hasProperty("my"))
        assertTrue(!project.hasProperty("my.prop"))
        assertEquals("def", System.getProperty("abc"))
        assertEquals("value", System.getProperty("my.prop"))
        assertEquals("value2", System.getProperty("my.setting"))
    }

}
