package me.gking2224.buildtools.tasks

import org.gradle.api.Project

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
        assert fromDir != null : "missing mandatory input: 'fromDir'"
        assert to != null : "missing mandatory input: 'to'"
        assert file != null : "missing mandatory input: 'file'"
        
        fromDir = project.resolveValue(fromDir)
        file = project.resolveValue(file)
        to = project.resolveValue(to)
        file = resolveFile()
    }
    
    @Override
    def _executeAction() {
        if (template) {
            templateObjects.putAll ([action:this, task:task])
            file = project.filteredFile(file, templateObjects)
        }
        def strCommand = "scp -P ${session.port} $file ${session.userName}@${session.host}:${to}"
        logger.info strCommand
        project.dryRunExecute(strCommand, {
            def ChannelSftp channel
            try {
                channel = session.openChannel("sftp")
                setStreams(channel)
                channel.connect()
                channel.put(file, to)
            }
            finally {
                logger.debug "Closing sftp channel"
                channel.close()
            }
        })
    }
    
    def resolveFile() {
        def f = project.fileNameFromParts fromDir, file
        def File ff = project.file(f)
        assert ff.exists()
        f
    }
    
    def templateObj(String name, Map m) {
        templateObjects[name] = m
    }
}