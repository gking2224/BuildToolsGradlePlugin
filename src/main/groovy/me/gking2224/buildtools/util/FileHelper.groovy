package me.gking2224.buildtools.util

import java.util.regex.Pattern

import org.slf4j.LoggerFactory

class FileHelper {
    
    def logger = LoggerFactory.getLogger(FileHelper.class)

    def static _instance = new FileHelper()
    
    def FileHelper() {
        
    }
    
    def static instance() {
        return _instance
    }
    
    def fileCollection(def dir, def file) {
        if (file == null) return []
        else if (Iterable.class.isAssignableFrom(file.getClass())) {
            return file.collect {asFile(dir, it)}
        }
        else return [asFile(dir, file)]
    }
    
    def asFile(def file) {
        asFile(null, file)
    }
    
    def asFile(def dir, def file) {
        if ([String,GString].any{it.isAssignableFrom(file.class)}) {
            def f = (dir == null)?new File(file):new File(dir, file)
            return f
        }
        else if (File.isAssignableFrom(file.class)) {
            return file
        }
        else if (Iterable.isAssignableFrom(file.class)){
            return file.collect {asFile(dir, it)}
        }
    }
    
    def filesAsString(def files) {
        if (files == null) return null
        else if ([String,GString].any {it.isAssignableFrom(files.class)}) {
            return files
        }
        else if (File.isAssignableFrom(files.class)) {
            return files.absolutePath
        }
        else if (Iterable.isAssignableFrom(files.class)) {
            
            if (files.size() == 1) {
                return filesAsString(files[0])
            }
            else {
                def sb = new StringBuilder("[")
                files.each{sb.append filesAsString(it); sb.append ", "}
                sb.substring(0, sb.length()-2) + "]"
            }
        }
    }
    
    def filesFromPattern(def dir, def pattern) {
        assert dir != null : "Null directory"
        assert pattern != null && pattern != "" : "Null pattern"
        
        dir = asFile(dir)
        
        logger.debug("filesFromPattern: dir=$dir ; pattern=$pattern")
        def rv = _filesFromPattern(dir, pattern, false)
        if (logger.isDebugEnabled()) {
            logger.debug "${rv.size()} matches:"
            rv.each {
                logger.debug("  ${it.absolutePath}")
            }
        }
        rv
    }
    
    def _filesFromPattern(def dir, def pattern, def recurse) {
        
        if (pattern.startsWith("/")) return _filesFromPattern(dir, pattern.substring(1), recurse)
        def split = splitFilePattern(pattern)
        def matchingDir = split[0]
        def remainingPattern = pattern
        
        // recursive search on next element
        if (matchingDir == "**")
            return _filesFromPattern(dir, split[1], true)
            
        // matching directory, non-recursive search on next element
        if (split[1] && dir.name ==~ matchingDir) {
            return _filesFromPattern(dir, split[1], false)
        }
        
        def rv = []
        // find matching files
        if (!split[1]) {
            dir.listFiles().findAll{
                it.name ==~ remainingPattern
            }.each{m->
                rv << m
            }
        }
        dir.listFiles().findAll{
            it.isDirectory() && (recurse || it.name ==~ matchingDir)
        }.each{
            _filesFromPattern(it, remainingPattern, recurse).each{m->
                rv << m
            }
        }
        rv
    }
    
    /**
     * Split a file pattern between initial directory and remaining pattern
     * @param pattern
     * @return
     */
    def splitFilePattern(def pattern) {
        
        def pp = Pattern.compile("([^/]*)/(.*)")
        def mm = pp.matcher(pattern)
        
        if (mm.matches()) [mm.group(1), mm.group(2)]
        else [pattern, null]
    }
}
