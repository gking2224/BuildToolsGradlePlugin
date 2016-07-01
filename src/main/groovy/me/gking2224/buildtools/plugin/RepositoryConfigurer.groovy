package me.gking2224.buildtools.plugin

import org.gradle.api.Project

class RepositoryConfigurer {

    def Project project
    
    def RepositoryConfigurer(Project p) {
        this.project = p
    }
    
    def configureRepos() {
        
        if (!project.hasProperty("mavenRepo")) {
            project.ext.mavenRepo = System.getProperty("user.home")+"/.m2/repository/"
        }
        def url = "file://localhost/${project.mavenRepo}"
        project.install {
            repositories {
                mavenDeployer {
                    repository(url: url)
                }
            }
        }
    }
}
