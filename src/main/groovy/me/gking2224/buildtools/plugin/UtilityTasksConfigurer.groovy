package me.gking2224.buildtools.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper

class UtilityTasksConfigurer {

    def Project project
    
    static final String DRY_RUN_PROPERTY = "dryRun"
    
    def UtilityTasksConfigurer(Project p) {
        this.project = p
    }
    
    def configureUtilityTasks() {
        
        loggingFunctions()
        
        project.ext.isDryRun = {
            if (!project.hasProperty(DRY_RUN_PROPERTY)) return false
            def dr = project.dryRun.toLowerCase()
            return ["true", "t", "", "yes", "y"].contains(dr)
        }
        
        project.ext.notRunning = {m ->
            project.info "DryRun - not running [{$m}]"
        }
        
        project.ext.dryRunExecute = {String message, Closure c->
            if (project.isDryRun()) {
                project.notRunning(message)
            }
            else {
                c()
            }
        }
        
        project.ext.resolveValue = {a ->
            if (a == null) return null
            else if (a instanceof Closure) {
                def val = a()
                return val
            }
            else return a
        }
        
        project.task("wrapper", type: Wrapper) {
            gradleVersion = '2.7'
        }
        
        project.ext.readProps = {File f->
            Properties props = new Properties()
            props.load(f.newDataInputStream())
            return props
        }
        
        project.ext.storeProps = {Properties p, File f->
            if (project.isDryRun()) project.notRunning("StoreProps ($p) to file ${f.absolutePath}")
            else p.store(f.newWriter(), null)
        }
        
        project.task("clearGradleCache") << {
            if (project.hasProperty("gradleCacheDir")) {
                project.file(project.gradleCacheDir).delete()
            }
        }
        
    }
    def loggingFunctions() {
        project.ext.debug = {m->
            project.logger.debug(m)
        }
        
        project.ext.info = {m->
            project.logger.info(m)
        }
        
        project.ext.error = {m->
            project.logger.error(m)
        }
        project.ext.trace = {m->
            project.logger.trace(m)
        }
        
    }
}
