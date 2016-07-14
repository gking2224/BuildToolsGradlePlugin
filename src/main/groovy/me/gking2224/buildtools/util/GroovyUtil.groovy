package me.gking2224.buildtools.util

import org.gradle.api.Project
import org.slf4j.LoggerFactory;

class GroovyUtil {

    def logger = LoggerFactory.getLogger(this.getClass())
    def static _instance
    
    def project
    
    def GroovyUtil(Project p) {
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
            logger.debug "<Start resolve closure value>"
            if (project) v.delegate = project
            def val = v()
            logger.debug "<End   resolve closure value : $val>"
            return val
        }
        else return v
    }
    
    def configureObjectFromClosure(def o, Closure c) {
        assert o != null
        assert c != null
        c.delegate = o
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        o
    }
    
}
