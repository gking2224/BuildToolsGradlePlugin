package me.gking2224.buildtools.tasks

import me.gking2224.buildtools.plugin.HasResolvableObjects;
import me.gking2224.buildtools.util.FileHelper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import bsh.This;

class Copy extends DefaultTask implements HasResolvableObjects {

    def fromDir
    def pattern
    def toDir
    
    def _groups = []
    
    @TaskAction
    def doCopy() {
        
        if (_groups.isEmpty()) _groups << new CopyGroup(fromDir, pattern, toDir)
        
        _groups.each {cg ->
            cg.toDir = FileHelper.instance().asFile(cg.toDir)
            cg.fromDir = FileHelper.instance().asFile(cg.fromDir)
            
            if (!cg.fromDir.exists()) logger.debug "$fromDir does not exists - nothing to do"
            else {
            
                if (!cg.toDir.exists()) cg.toDir.mkdirs()
                def files = project.filesFromPattern(cg.fromDir, cg.pattern)
                
                files.each{File f->
                    def stub = f.parentFile.absolutePath.substring(cg.fromDir.absolutePath.length())
                    File to = FileHelper.instance().asFile(project.fileNameFromParts(cg.toDir, stub, f.name))
                    def ff = project.filteredFile(f, [task:this])
                    project.dryRunExecute("Not copying $f.absolutePath to $to.absolutePath", {
                        if (!to.parentFile.exists()) to.parentFile.mkdirs()
                        ff.withDataInputStream {dis->
                            to.withDataOutputStream {dos->
                                dos << dis
                            }
                        }
                        logger.info "Copied $f.absolutePath to $to.absolutePath"
                    })
                }
            }
        }
    }
    
    def group(Closure c) {
        assert !([fromDir, pattern, toDir].any {it != null}) : "Cannot add group if fromDir, pattern or toDir already set"
        def cg = new CopyGroup()
        c.delegate = cg
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        
        _groups << cg
    }

    @Override
    public void resolveObjects() {
        _groups = _groups.each{it.resolveValues(project)}
    }
    
    class CopyGroup {
        def fromDir
        def pattern
        def toDir
        
        def CopyGroup() {
            
        }
        def CopyGroup(def fromDir, def pattern, def toDir) {
            this.fromDir = fromDir
            this.pattern = pattern;
            this.toDir = toDir
        }
        def resolveValues(def project) {
            fromDir = project.resolveValue(fromDir)
            pattern = project.resolveValue(pattern)
            toDir = project.resolveValue(toDir)
        }
        
        String toString() {
            "CopyGroup $fromDir $pattern $toDir"
        }
    }
}

