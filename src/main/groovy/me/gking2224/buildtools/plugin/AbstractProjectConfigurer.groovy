package me.gking2224.buildtools.plugin

import org.gradle.api.Project
import org.slf4j.LoggerFactory;

abstract class AbstractProjectConfigurer {

    def logger = LoggerFactory.getLogger(this.class)
    def Project project 
    
    public AbstractProjectConfigurer(Project p) {
        this.project = p
    }

}
