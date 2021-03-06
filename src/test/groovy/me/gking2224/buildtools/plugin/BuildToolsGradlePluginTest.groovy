package me.gking2224.buildtools.plugin

import static org.junit.Assert.*

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test


class BuildToolsGradlePluginTest {
 
    def project
    
    @Before
    void before() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply "java"
        project.pluginManager.apply "maven"
        project.pluginManager.apply BuildToolsGradlePlugin.NAME
        project.applyBuildTools()
        project.ext["buildtools.environment"] = "test"
        
    }
    
    @Test
    public void resolveValue() {
        assertEquals( "string", project.resolveValue("string") )
        assertNull( project.resolveValue(null) )
        def n = "value_of_n"
        def c = { return n }
        assertEquals(n, project.resolveValue(c) )
    }
    
    @Test
    public void featureEnablement() {
        project.buildtools {
            myfeatureEnabled()
        }
        assertTrue(project.featureEnabled("myfeature"))
        assertFalse(project.featureEnabled("yourfeature"))
    }
}
