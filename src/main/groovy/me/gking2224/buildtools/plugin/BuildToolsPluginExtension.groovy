package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.GroovyUtil
import me.gking2224.common.utils.NestedProperties
import me.gking2224.common.utils.PrefixedProperties

import org.gradle.api.Project
import org.slf4j.LoggerFactory

class BuildToolsPluginExtension {
    
    def logger = LoggerFactory.getLogger(BuildToolsPluginExtension.class)

    static final KEY = "buildtools"
    
    def Project project
    
    def features = [:]
    
    public BuildToolsPluginExtension(Project project) {
        this.project = project
        features.eclipse = true
        project.ext.featureEnabled = {name ->
            if (!features[name]) return false
            else return features[name]
        }
    }
    
    def methodMissing(String name, value) {
        if (name.endsWith("Enabled")) {
            def feature = name.substring(0, name.length() - 7)
            features[feature] = true
        }
        else if (name.endsWith("Disabled")) {
            def feature = name.substring(0, name.length() - 8)
            features[feature] = false
        }
    }
}
