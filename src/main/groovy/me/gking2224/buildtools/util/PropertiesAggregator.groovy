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
        o.each {k,v->
            properties[k] = [:]
            if (v != null && Map.isAssignableFrom(v.getClass())){
                properties[k].putAll populatePrefixedProperties(properties, k, v)
            }
            else if (v != null && v.hasProperty("properties")) {
                properties[k].putAll populatePrefixedProperties(properties, k, v.getProperties())
            }
            if (v != null && v.hasProperty("ext") && DefaultExtraPropertiesExtension.class.isAssignableFrom(v.ext.getClass())) {
                properties[k].putAll populatePrefixedProperties(properties, k, v.ext.getProperties())
            }
        }
        properties
    }
    
    def populatePrefixedProperties(Map properties, def prefix, def m) {
        def rv = [:]
        m.each {k,v->
            if (v == null) v = ""
            if (k == "properties" || k == "ext") v = "<removed $k>"
            if ([String,GString].any {it.isAssignableFrom(v.getClass())}) {
                rv[k] = v
                if (!properties.containsKey(k)) properties[k] = v
                else if (properties[k] != v){
                    rv[k] = CONFLICT
                }
            }
            else if (prefix.startsWith("envProps") && Closure.isAssignableFrom(v.getClass())) {
                rv[k] = GroovyUtil.instance().resolveValue(v)
            }
            else if (Map.isAssignableFrom(v.getClass())) {
                rv[k] = populatePrefixedProperties(properties, "$prefix.$k", v)
            }
            else {
                logger.debug "not adding $k with value of type ${v.getClass()}"
            }
            properties["$prefix.$k"] = rv[k]
        }
        return rv
    }

}
