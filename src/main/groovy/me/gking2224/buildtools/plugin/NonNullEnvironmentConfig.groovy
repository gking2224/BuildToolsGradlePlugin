package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.GroovyUtil

import org.gradle.api.Project

class NonNullEnvironmentConfig extends EnvironmentConfig {

    def fallbackValue

    def NonNullEnvironmentConfig(String configPath, Project project, String fallbackValue) {
        super(configPath, project, null);
        this.fallbackValue = fallbackValue
    }

    def NonNullEnvironmentConfig(String configPath, Project project) {
        this(configPath, project, "<<missing>>");
    }

    def NonNullEnvironmentConfig(String configPath, String fallbackValue) {
        this(configPath, null, fallbackValue);
    }

    def NonNullEnvironmentConfig(String configPath) {
        this(configPath, null)
    }

    def NonNullEnvironmentConfig() {
        this("<root>")
    }
    
    @Override
    def createSubConfigFromClosureWithPath(def c, def subPath) {
        EnvironmentConfig ec = new NonNullEnvironmentConfig(subPath, project, fallbackValue)
        GroovyUtil.instance().configureObjectFromClosure(ec, c)
        ec
    }
    
    @Override
    def propertyMissing(String name) {
        def rv = super.propertyMissing(name)
        if (!rv) {
            rv = fallbackValue
            logger.debug "<Using fallback for    $configPath.$name: $fallbackValue"
        }
        rv
    } 
}
