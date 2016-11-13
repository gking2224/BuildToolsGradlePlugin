package me.gking2224.buildtools.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class BuildToolsGradlePlugin implements Plugin<Project> {

    static final String NAME = "me.gking2224.buildtools"
    static final String GROUP = "Build Tools"
    static final String GRADLE_PROPERTIES_FILE = "gradle.properties"
    
    def Project project
    
    def configurers = []

	void apply(Project p) {
        
        this.project = p
        // declare envs extension
        project.extensions.create(
            EnvironmentsHandler.KEY,
             EnvironmentsHandler, project)
        project.extensions.create(
            BuildToolsPluginExtension.KEY,
             BuildToolsPluginExtension, project)
        
        project.ext.applyBuildTools = {
            configureProject()
        }
//        configurers << new ProjectLifecycleConfigurer(project)
        configurers << new UtilityTasksConfigurer(project)
        configurers << new DefaultProjectConfigurer(project)
        configurers << new SecretPropertiesConfigurer(project)
        configurers << new DirectoriesConfigurer(project)
        configurers << new RepositoryConfigurer(project)
        configurers << new IntegrationTestConfigurer(project)
        configurers << new ReleaseTasksConfigurer(project)
        
        
	}
    
    def configureProject() {
        configurers.each {c->
            c.configureProject()
        }
    }
}

