package me.gking2224.buildtools.plugin

import org.gradle.api.internal.plugins.DefaultConvention
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

class EnvironmentConfig {
    
    def props = [:]
    
    def project
    
    def EnvironmentConfig(def project) {
        this.project = project
//        def mc = new ExpandoMetaClass( EnvironmentConfig, false, true)
//        mc.initialize()
//        this.metaClass = mc
    }

    def propertyMissing(String name, def value ) {
        props[name] = value
    }

    def methodMissing(String name, args) {
        assert args.length == 1 : "Environment config value for $name must be single item"
        def arg = args[0]
        
        if (Closure.isAssignableFrom(arg.getClass())) {
            props[name] = EnvironmentConfig.fromClosure(arg, project).props
        }
        else {
            props[name] = arg
        }
        
    }
    
    static def fromClosure(Closure c, def p) {
        EnvironmentConfig ec = new EnvironmentConfig(p)
        c.delegate = ec
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        ec
    }
}
