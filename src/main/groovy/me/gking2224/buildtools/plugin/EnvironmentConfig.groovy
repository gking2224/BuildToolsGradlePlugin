package me.gking2224.buildtools.plugin

import org.gradle.api.internal.plugins.DefaultConvention
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

class EnvironmentConfig {
    
    def props = [:]
    
    def EnvironmentConfig() {
    }

    def methodMissing(String name, args) {
        assert args.length == 1 : "Environment config value for $name must be single item"
        def arg = args[0]
        props[name] = arg
    }
}
