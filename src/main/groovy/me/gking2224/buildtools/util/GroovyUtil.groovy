package me.gking2224.buildtools.util

import org.gradle.api.Project

class GroovyUtil {

    def static _instance
    
    def project
    
    def GroovyUtil(Project p) {
        assert p != null
        project = p
    }
    
    static def instance(Project p) {
        if (_instance == null) _instance = new GroovyUtil(p)
        return _instance
    }
    
    static def instance() {
        instance(null)
    }
    
    def resolveValue(def v) {
        
        if (v == null) return null
        else if (v instanceof Closure) {
            v.delegate = project
            def val = v()
            return val
        }
        else return v
    }
    
}
