package me.gking2224.buildtools.util

import org.gradle.api.Project

class PropertiesResolver {

    static final String SYSTEM_PREFIX = "systemProp."
    
    def Project project
    
    def PropertiesResolver(Project p) {
        this.project = p
    }
    def resolveProperties(Properties props) {
        def Map m = new HashMap()
        props.each{k,v->
            storeProperty(k, v, m)
        }
        m.each{k,v-> project.ext[k] = v}
    }
    def subtractSystemPrefix(String k) {
        return k.substring(SYSTEM_PREFIX.length())
    }
    
    def storeProperty(String k, String v, def m) {
        
        if (k.startsWith(SYSTEM_PREFIX)) {
            System.setProperty(subtractSystemPrefix(k), v)
        }
        else {
            m.put(k, v)
        }
    }
}
