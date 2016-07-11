package me.gking2224.buildtools.plugin

import groovy.text.StreamingTemplateEngine
import groovy.text.Template
import me.gking2224.buildtools.util.FilePath
import me.gking2224.buildtools.util.PropertiesAggregator
import me.gking2224.buildtools.util.RandomString

import org.gradle.api.Project

class UtilityTasksConfigurer extends AbstractProjectConfigurer {

    static final String DRY_RUN_PROPERTY = "dryRun"
    static final String RETAIN_FILTERED_FILES_PROPERTY = "retainFilteredFiles"
    
    def UtilityTasksConfigurer(Project p) {
        super(p)
    }
    
    def configureProject() {
        
        loggingFunctions()
        utilityFunctions()
        dryRun()
        tasks()
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
    
    def utilityFunctions() {
        
        project.ext.booleanProperty = {p->
            if (!project.hasProperty(p)) return false
            def v = project[p].toLowerCase()
            return ["true", "t", "", "yes", "y"].contains(v)
        }
        project.ext.resolveValue = {a ->
            if (a == null) return null
            else if (a instanceof Closure) {
                def val = a()
                return val
            }
            else return a
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
        
        project.ext.fileNameFromParts = {Object... o->
            FilePath.pathFromParts(o)
        }
        
        project.ext.withinTimeout = {Long startTime, Long timeoutMillis->
            (System.currentTimeMillis() - startTime) <= timeoutMillis
        }
        
        project.ext.randomString = {Long length ->
            if (length == null || length == 0) length = 16
            def s = new RandomString(length).toString()
        }
        project.ext.filteredFiles = []
        if (!project.booleanProperty(RETAIN_FILTERED_FILES_PROPERTY)) {
            project.gradle.buildFinished {result->
                if (!project.filteredFiles.isEmpty()) {
                    if (result.failure == null) {
                        logger.debug "Removing filtered files - set flag -P$RETAIN_FILTERED_FILES_PROPERTY to stop this"
                        project.filteredFiles.each {it.deleteDir()}
                    }
                    else {
                        logger.error "Build failed so not removing filtered files"
                    }
                }
            }
        }
        project.ext.filteredFile = {def f, def objects->
            logger.debug("filteredFile: $f, $objects")
            if (f == null) return null
            else if (Iterable.class.isAssignableFrom(f.class)) {
                return f.collect {project.filteredFile(it, objects)}
            }
            
            def asString = [String,GString].any { it.isAssignableFrom(f.class) }
            if (asString) {
                f = new File(f)
            }
            objects["project"] = project
            
            def dir = new File(project.fileNameFromParts(project.runDir, project.randomString(16)))
            dir.mkdirs()
            project.filteredFiles << dir
            
            File ff = new File(dir, f.name)
            
            def Template template = new StreamingTemplateEngine().createTemplate(new FileReader(f))
            template.
            def binding = new PropertiesAggregator().aggregate(objects)
            def fw = new FileWriter(ff)
            fw.write(template.make(binding))
            fw.close()
            
            logger.info "Created filtered file: ${ff.absolutePath}"
            
            return (asString)?ff.absolutePath:ff
        }
    }
    
    def dryRun() {
        project.ext.isDryRun = {
            project.booleanProperty DRY_RUN_PROPERTY
        }
        
        project.ext.notRunning = {m ->
            project.info "DryRun - not running [{$m}]"
        }
        
        project.ext.dryRunExecute = {String message, Closure... c->
            assert (c.length == 1|| c.length == 2) : "Need one or two closure to be provided"
            if (project.isDryRun()) {
                project.notRunning(message)
                if (c.length == 2) {
                    c[1]()
                } 
            }
            else {
                c[0]()
            }
        }
    }
    
    def tasks() {
        project.task("clearGradleCache") << {
            if (project.hasProperty("gradleCacheDir")) {
                project.file(project.gradleCacheDir).delete()
            }
        }
    }
}
