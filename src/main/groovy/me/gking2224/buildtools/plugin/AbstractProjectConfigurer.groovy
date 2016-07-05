package me.gking2224.buildtools.plugin

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractProjectConfigurer implements ProjectConfigurer {

    Logger logger = LoggerFactory.getLogger(this.class)
    def Project project 
    
    public AbstractProjectConfigurer(Project p) {
        this.project = p
    }

}
