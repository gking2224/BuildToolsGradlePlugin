package me.gking2224.buildtools.plugin

import org.gradle.api.Project

class RepositoryConfigurer extends AbstractProjectConfigurer {
    
    static final String DEFAULT_LOCAL_MAVEN_REPO = "file://localhost/${System.getProperty('user.home')}/.m2/repository/"
    
    def RepositoryConfigurer(Project p) {
        super(p)
    }
    
    def configureProject() {
        if (project.featureEnabled("maven")) {
            if (!project.pluginManager.hasPlugin("maven")) {
                project.pluginManager.apply "maven"
            }
            if (!project.hasProperty("localMavenRepo")) {
                logger.debug("No property 'localMavenRepo' defined, using default value: $DEFAULT_LOCAL_MAVEN_REPO")
                project.ext.localMavenRepo = DEFAULT_LOCAL_MAVEN_REPO
            }
            project.install {
                repositories {
                    mavenDeployer {
                        repository(url: project.localMavenRepo)
                    }
                }
            }
        }
    }
}
