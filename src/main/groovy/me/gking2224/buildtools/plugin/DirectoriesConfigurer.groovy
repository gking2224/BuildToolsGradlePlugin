package me.gking2224.buildtools.plugin

import org.gradle.api.Project

class DirectoriesConfigurer extends AbstractProjectConfigurer {
    
    public DirectoriesConfigurer(Project p) {
        super(p)
    }

    def configureProject() {
        
        def scriptsDir = project.file("scripts")
        project.ext.scriptsDir = scriptsDir
        
        project.ext.runDir = project.file(project.fileNameFromParts(project.buildDir, project.runId))
        project.runDir.mkdirs()
        logger.debug("Created runDir: $project.runDir")
    }
}
