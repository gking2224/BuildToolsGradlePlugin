package me.gking2224.buildtools.util

import org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PropertiesAggregator {

    static final def CLOSURE = "<<closure>>"
    static final def CONFLICT = "<<conflict>>"
    def Logger logger = LoggerFactory.getLogger(PropertiesAggregator.class)
    
    public PropertiesAggregator() {
    }
    def aggregate(Map o) {
        def l = logger
        def properties = [:]
        logger.debug("Adding PropertiesAggregator properties:")
        o.each {k,v->
            if (v != null && Map.isAssignableFrom(v.getClass())){
                populatePrefixedProperties(properties, k, v)
            }
            else if (v != null && v.hasProperty("properties")) {
                populatePrefixedProperties(properties, k, v.getProperties())
            }
            if (v != null && v.hasProperty("ext") && DefaultExtraPropertiesExtension.class.isAssignableFrom(v.ext.getClass())) {
//                populatePrefixedProperties ext = (DefaultExtraPropertiesExtension)(v.ext)
                populatePrefixedProperties(properties, k, v.ext.getProperties())
            }
        }
        if (logger.debugEnabled) {
            logger.debug("PropertiesAggregator created with: {$properties}")
        }
        properties
    }
    
    def populatePrefixedProperties(Map properties, def prefix, def m) {
        m.each {k,v->
            if (properties[prefix] == null) properties[prefix] = [:]
            if (v != null && Closure.isAssignableFrom(v.getClass())) v = CLOSURE
            if (k == "properties" || k == "ext") v = "<removed $k>"
            if ([String,GString].any {it.isAssignableFrom(v.getClass())}) {
                properties[prefix][k] = v
                
                if (!properties.containsKey(k)) properties[k] = v
                else if (properties[k] != v){
                    logger.warn("Got conflicted key: $k")
                    properties[k] = CONFLICT
                }
            }
            else {
                logger.debug "not adding $k with value of type ${v.getClass()}"
            }
        }
    }

}
