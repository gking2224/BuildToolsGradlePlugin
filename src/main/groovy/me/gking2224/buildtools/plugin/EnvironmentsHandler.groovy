package me.gking2224.buildtools.plugin

import groovy.lang.Closure;

import org.gradle.api.Project

class EnvironmentsHandler {

    static final KEY = "environments"
    
    def Project project
    
    public EnvironmentsHandler(Project project) {
        this.project = project
    }
    
    def env(String env, Closure c) {
        def sysEnv = System.getProperty("buildtools.environment")
        println ("System property 'buildtools.environment': $sysEnv ... ${project.buildtools.environment}")
        if (sysEnv == env) {
            EnvironmentConfig ec = new EnvironmentConfig()
            c.delegate = ec
            c()
            project.ext.environment = ec
        }
    }
}
