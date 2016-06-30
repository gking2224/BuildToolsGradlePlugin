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
        def idx = k.indexOf(".")
        if (idx != -1) {
            def p1 = k.substring(0, idx)
            def p2 = k.substring(idx+1)
            def submap = getSubMapForKey(p1, m)
            m.put(p1, submap)
            storeProperty(p2, v, submap)
        }
    }
    
    def getSubMapForKey(def k, Map m) {
        if (project.hasProperty(k)) {
            def v = project[k]
            if (!(v instanceof Map)) {
                throw new IllegalStateException("Overloading of property $k on project")
            }
            else {
                return v
            }
        }
        else {
            def v = m.get(k)
            if (v != null) {
                if (!(v instanceof Map)) {
                    throw new IllegalStateException("Overloading of property $k on project")
                }
                else {
                    return (Map)v
                }
            }
            else {
                return new HashMap<String,Object>()
            }
            
        }
    }
}
