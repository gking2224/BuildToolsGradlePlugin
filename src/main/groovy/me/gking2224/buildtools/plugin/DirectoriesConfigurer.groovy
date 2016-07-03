package me.gking2224.buildtools.plugin

import org.gradle.api.Project

class DirectoriesConfigurer {

    def Project project
    
    public DirectoriesConfigurer(Project p) {
        this.project = p
    }

    def configureDirectories() {
        project.ext.scriptsDir = project.file("scripts")
        project.ext.runDir = project.file(project.fileNameFromParts(project.buildDir, project.runId))
        project.runDir.mkdirs()
    }
}
