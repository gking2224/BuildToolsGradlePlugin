package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.PropertiesResolver

import org.gradle.api.Plugin
import org.gradle.api.Project

public class BuildToolsGradlePlugin implements Plugin<Project> {

    static final String NAME = "me.gking2224.buildtools"
    static final String GROUP = "Build Tools"
    static final String GRADLE_PROPERTIES_FILE = "gradle.properties"
    static final String SECRET_PROPERTIES_FILE = "secret.properties"
    
    def Project project

	void apply(Project p) {
        this.project = p
        // declare envs extension
        project.extensions.create(
            EnvironmentsHandler.KEY,
             EnvironmentsHandler, project)
        
        
        if (project.file(SECRET_PROPERTIES_FILE).exists()) {
            new PropertiesResolver(p).resolveProperties(readProps(project.file(SECRET_PROPERTIES_FILE)))
        }
        
        new UtilityTasksConfigurer(project).configureUtilityTasks()
        
        new RepositoryConfigurer(project).configureRepos()
        
        new IntegrationTestConfigurer(project).configureIntegrationTests()
        new ReleaseTasksConfigurer(project).configureReleaseTasks()
		
	}
}

