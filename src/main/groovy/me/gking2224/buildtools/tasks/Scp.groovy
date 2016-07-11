package me.gking2224.buildtools.tasks

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

import com.jcraft.jsch.ChannelSftp

class Scp extends RemoteAction {
    def fromDir
    def to
    def file
    def template = true
    def templateObjects = [:]
    
    def Scp(Project p) {
        super(p)
    }
    
    @Override
    def validate() {
        assert file != null : "missing mandatory input: 'file'"
        file = project.resolveValue(file)
        assert to != null : "missing mandatory input: 'to'"
        
        fromDir = project.resolveValue(fromDir)
        to = project.resolveValue(to)
        file = fileHelper.fileCollection(fromDir, file)
        file.each{assert it.exists()}
    }
    
    @Override
    def _executeAction() {
        if (template) {
            templateObjects.putAll ([action:this, task:task])
            file = file.collect {project.filteredFile(it, templateObjects)}
        }
        def filesStr = fileHelper.filesAsString(file)
        
        logger.debug("Files: "+file)
        def strCommand = "scp -P ${session.port} $filesStr ${session.userName}@${session.host}:${to}"
        logger.info strCommand
        project.dryRunExecute(strCommand, {
            def ChannelSftp channel
            try {
                channel = session.openChannel("sftp")
                setStreams(channel)
                channel.connect()
                file.each{
                    channel.put(it.absolutePath, to)
                }
            }
            finally {
                logger.debug "Closing sftp channel"
                channel.close()
            }
        })
    }
    
    def templateObj(String name, Map m) {
        templateObjects[name] = m
    }
    
}