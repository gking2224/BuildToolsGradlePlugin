package me.gking2224.buildtools.plugin

import groovy.lang.Closure;
import me.gking2224.buildtools.util.GroovyUtil;

import org.gradle.api.Project
import org.slf4j.LoggerFactory;

class EnvironmentsHandler {
    
    def logger = LoggerFactory.getLogger(EnvironmentsHandler.class)

    static final KEY = "environments"
    
    def Project project
    
    def EnvironmentConfig all
    
    public EnvironmentsHandler(Project project) {
        this.project = project
        all = new NonNullEnvironmentConfig("all", project, "<<unconfigured>>")
        project.ext.envProps = {}
        
    }
    
    def env(String env, Closure c) {
        assert project.hasProperty("env")
        def sysEnv = project.env
        
        if (env == "all") {
            GroovyUtil.instance().configureObjectFromClosure(all, c)
        }
        if (sysEnv == env) {
            def ec = new EnvironmentConfig(env, project, all)
            ec = GroovyUtil.instance().configureObjectFromClosure(ec, c)
            project.ext.envProps = ec
        }
    }
}
