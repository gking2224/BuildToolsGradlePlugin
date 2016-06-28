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
        println "Configuring environment $env"
        if (System.getProperty("buildtools.environment") == env) {
            EnvironmentConfig ec = new EnvironmentConfig()
            c.delegate = ec
            c()
            project.ext.environment = ec
        }
        else {
            println "not processing env config for $env"
        }
    }
}
