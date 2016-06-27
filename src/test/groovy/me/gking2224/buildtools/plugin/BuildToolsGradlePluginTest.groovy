package me.gking2224.buildtools.plugin

import static org.junit.Assert.*
import me.gking2224.buildtools.util.Version

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test


class BuildToolsGradlePluginTest {
 
    def project
    
    @Before
    void before() {
        project = ProjectBuilder.builder().build()
//        project.pluginManager.apply BuildToolsGradlePlugin.NAME
    }
    @Test
    public void testTaskDefined() {
        
//        assertTrue(project.tasks.bumpVersion != null)
    }
    
    @Test
    void testIncrementVersion() {
//        def v = new Version("1.0.0-SNAPSHOT")
//        assertEquals("1.0.1-SNAPSHOT", v.rawVersion)
        
    }
    
}
