package me.gking2224.buildtools.tasks

import me.gking2224.buildtools.util.GitHelper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GitCommit extends DefaultTask {

    def pattern
    def message
    def GitHelper gitHelper
    
    @TaskAction
    def doCommit() {
        gitHelper = GitHelper.getInstance(project)
        if (project.isDryRun()) project.notRunning("GitCommit pattern=$pattern message=$message")
        else gitHelper.commitFile(project.rootDir, pattern, message)
    }
}
