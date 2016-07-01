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
        
        project.ext.isDryRun = {
            if (!project.hasProperty(DRY_RUN_PROPERTY)) return false
            def dr = project.dryRun.toLowerCase()
            return ["true", "t", "", "yes", "y"].contains(dr)
        }
        
        project.ext.notRunning = {m ->
            println "DryRun - not running{$m}"
        }
        
        project.ext.resolve = {a ->
            if (a == null) return null
            else if (a instanceof Closure) return a()
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
        
        project.extensions.storeProps = {Properties p, File f->
            if (project.isDryRun()) project.notRunning("StoreProps ($p) to file ${f.absolutePath}")
            else p.store(f.newWriter(), null)
        }
        
        project.task("clearGradleCache") << {
            if (project.hasProperty("gradleCacheDir")) {
                project.file(project.gradleCacheDir).delete()
            }
        }
        
    }
}
