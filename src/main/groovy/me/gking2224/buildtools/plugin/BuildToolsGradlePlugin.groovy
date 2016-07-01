package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.tasks.GitCommit
import me.gking2224.buildtools.util.GitHelper
import me.gking2224.buildtools.util.PropertiesResolver;
import me.gking2224.buildtools.util.Version

import org.eclipse.jgit.api.Status
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.wrapper.Wrapper

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
        
        new RepositoryConfigurer(project).configureRepos()
        
        project.ext.isDryRun = {
            if (!project.hasProperty("dryRun")) return false
            def dr = project.dryRun.toLowerCase()
            return ["true", "t", "", "yes", "y"].contains(dr)
        }
        project.ext.notRunning = {m ->
            println "DryRun - not running{$m}"
        }
        new IntegrationTestConfigurer(project).configureIntegrationTests()
        new ReleaseTasksConfigurer(project).configureReleaseTasks()
		
        project.task("wrapper", type: Wrapper) {
            gradleVersion = '2.7'
        }
        project.ext.resolve = {a ->
            if (a == null) return null
            else if (a instanceof Closure) return a()
            else return a
        }
        project.task("clearGradleCache") << {
            if (project.hasProperty("gradleCacheDir")) {
                project.file(project.gradleCacheDir).delete()
            }
        }
	}
}

