package me.gking2224.buildtools.util

import org.slf4j.LoggerFactory;

class FileHelper {
    
    def logger = LoggerFactory.getLogger(FileHelper.class)

    def static _instance = new FileHelper()
    
    def FileHelper() {
        
    }
    
    def static instance() {
        return _instance
    }
    
    def fileCollection(def dir, def file) {
        logger.debug("fileCollection $dir $file")
        if (file == null) return []
        else if (Iterable.class.isAssignableFrom(file.getClass())) {
            return file.collect {asFile(dir, it)}
        }
        else return [asFile(dir, file)]
    }
    
    def asFile(def dir, def file) {
        logger.debug("asFile $dir $file")
        if ([String,GString].any{it.isAssignableFrom(file.class)}) {
            def f = (dir == null)?new File(file):new File(dir, file)
            logger.debug("String asFile: $dir $file :: $f.absolutePath")
            return f
        }
        else if (File.isAssignableFrom(file.class)) {
            logger.debug("Ignoring dir $dir")
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
}
