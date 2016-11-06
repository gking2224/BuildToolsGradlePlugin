package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.GroovyUtil

import org.gradle.api.Project
import org.slf4j.LoggerFactory;

class PropertiesEnvironmentConfig {
    
    static final PLACEHOLDER_START = "\${"
    static final PLACEHOLDER_END = "}"
    
    def logger = LoggerFactory.getLogger(this.getClass())
    
    def resolvers = []
    def project
    def env
    
    def PropertiesEnvironmentConfig(String env, Project project, List<Properties> props) {
        this.env = env
        this.project = project
        this.resolvers << {n ->
             return System.getenv(n)
         }
        this.resolvers << {n ->
            return System.getProperty(n)
        }
        props.each {p ->
            this.resolvers << { n ->
                 return p.getProperty(n)
            }
        } 
    }
    
    def String toString() {
        "EnvironmentConfig $configPath"
    }

    def propertyMissing(String name, def value) {
        System.out.println("property missing; set (no-op) " + name + "=" + value)
    }
    
    def propertyMissing(String name) {
        return resolveValue(name)
    }
    
    def resolveValue(String name) {
        logger.debug("Looking for $name")
        def rv = null
        this.resolvers.each { resolve -> 
            def value = resolve(name)
            if (value != null) {
                rv = resolveNestedProperties(value)
                logger.debug("using value $name=$rv")
            }
        }
        if (rv == null) logger.debug("Could not find value for $name")
        return rv
    }
    
    def resolveNestedProperties(String value) {
        return resolveNestedProperties(value, 0);
    }
    
    def resolveNestedProperties(String value, int startFrom) {
        int idx = value.indexOf(PLACEHOLDER_START, startFrom)
        if (idx == -1) return value
        int matching = value.indexOf(PLACEHOLDER_END, idx)
        
        String placeholder = value.substring(idx, matching+1)
        String propName = getPropName(placeholder)
        String defaultValue = getDefaultValue(placeholder)
        
        String replacedValue = resolveValue(propName)
        if (replacedValue == null) {
            if (defaultValue != null) replacedValue = defaultValue
            else throw new IllegalArgumentException("Could not resolve property $value")
        }
        def updated = value.replace(placeholder, replacedValue)
        return resolveNestedProperties(updated, idx + replacedValue.length())
    }
    
    def getPropName(String value) {
        def idx = value.indexOf(":")
        if (idx == -1)
            return value.substring(2, value.length()-1)
        else return value.substring(2, idx)
    }
    
    def getDefaultValue(String value) {
        def idx = value.indexOf(":")
        if (idx == -1)
            return null
        else return value.substring(idx+1, value.length()-1)
    }
}
