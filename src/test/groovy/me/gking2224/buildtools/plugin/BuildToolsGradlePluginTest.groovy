package me.gking2224.buildtools.plugin

import static org.junit.Assert.*

import java.time.chrono.JapaneseChronology;

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
        
    }
    @Test
    public void testTaskDefined() {
        
        assertTrue(project.tasks.release != null)
    }
    
    @Test
    public void testEnvironmentsConfig() {
        
        project.environments {
            env("test") {
                url = "http://testenv/"
            }
            env("prod") {
                url = "http://prod/"
            }
        }
        assert project.environment.url == "http://testenv/"
        assert project.environment instanceof EnvironmentConfig
    }
}
