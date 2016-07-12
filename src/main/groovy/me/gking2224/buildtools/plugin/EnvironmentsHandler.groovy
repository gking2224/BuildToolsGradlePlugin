package me.gking2224.buildtools.plugin

import groovy.lang.Closure;

import org.gradle.api.Project
import org.slf4j.LoggerFactory;

class EnvironmentsHandler {
    
    def logger = LoggerFactory.getLogger(EnvironmentsHandler.class)

    static final KEY = "environments"
    
    def Project project
    
    public EnvironmentsHandler(Project project) {
        this.project = project
    }
    
    def env(String env, Closure c) {
        assert project.hasProperty("env")
        def sysEnv = project.env
        if (sysEnv == env) {
            project.ext.envProps = EnvironmentConfig.fromClosure(c, project).props
        }
    }
}
