package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.GroovyUtil

import org.gradle.api.Project
import org.slf4j.LoggerFactory;

class EnvironmentConfig {
    
    def logger = LoggerFactory.getLogger(this.getClass())
    def props = [:]
    
    def project
    def fallback
    def configPath
    
    def EnvironmentConfig(String configPath, Project project, EnvironmentConfig fallback) {
        this.configPath = configPath
        this.project = project
        this.fallback = fallback
    }
    
    def EnvironmentConfig(String configPath, EnvironmentConfig fallback) {
        this(configPath, null, fallback)
    }
    
    def EnvironmentConfig(String configPath, Project project) {
        this(configPath, project, null)
    }
    
    def EnvironmentConfig(String configPath) {
        this(configPath, null)
    }
    
    def EnvironmentConfig() {
        this("<root>")
    }

    def propertyMissing(String name, def value) {
        props[name] = value
    }
    
    def propertyMissing(String name) {
        logger.debug "<Start search property $configPath.$name>"
        def rv = propertyValueOrFallback(name)
        logger.debug "<End   search property $configPath.$name : $rv>"
        rv
    } 
    
    def propertyValueOrFallback(def name) {
        
        def rv = props[name]
        if (!rv) {
            if (fallback) {
                logger.debug "Property $name NOT found in $configPath; looking in fallback"
                rv = fallback[name]
            }
            else {
                logger.debug "Property $name NOT found in $configPath, no fallback"
            }
        }
        else {
            logger.debug "<Start resolve $configPath.$name>"
            rv = GroovyUtil.instance().resolveValue(rv)
            logger.debug "<End resolve   $configPath.$name>"
        }
        if (rv && EnvironmentConfig.isAssignableFrom(rv.getClass()) && fallback) {
            logger.debug "Checking if fallback should be retain for deeper property failures"
            def fbValue = fallback[name]
            if (fbValue && EnvironmentConfig.isAssignableFrom(fbValue.getClass())) {
                logger.debug "Retaining fallback ${fbValue.configPath} for ${rv.configPath}"
                rv.fallback = fbValue
            }
            else {
                logger.debug "Retaining fallback ${this.fallback.configPath} for ${rv.configPath}"
                rv.fallback = this.fallback // questionable
            }
        }
        rv
    }

    def methodMissing(String name, args) {
        assert args.length == 1 : "Environment config value for $name must be single item"
        def arg = args[0]
        
        if (Closure.isAssignableFrom(arg.getClass())) {
            props[name] = createSubConfigFromClosureWithPath(arg, "$configPath.$name")
        }
        else {
            props[name] = arg
        }
        props[name]
        
    }
    
    def createSubConfigFromClosureWithPath(def c, def subPath) {
        EnvironmentConfig ec = new EnvironmentConfig(subPath, project)
        GroovyUtil.instance().configureObjectFromClosure(ec, c)
        ec
    }
    
//    static def fromClosure(String configPath, Closure c) {
//        fromClosure(configPath, c, null)
//    }
//    
//    static def fromClosure(String configPath, Closure c, def p) {
//        
//    }
    
    def String toString() {
        "EnvironmentConfig $configPath"
    }
}
