package me.gking2224.buildtools.tasks

import me.gking2224.buildtools.util.FileHelper

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.jcraft.jsch.Channel
import com.jcraft.jsch.Session

abstract class RemoteAction {
    
    protected static final String SUDO_PASSWORD_PROMPT = ""
    protected static final String SUDO_PASSWORD_ERROR = "${SUDO_PASSWORD_PROMPT}Sorry, try again."
    
    Logger logger = LoggerFactory.getLogger(this.class)
    
    def Project project
    def Session session
    
    def InputStream stdout
    def InputStream stderr
    def BufferedReader stdoutReader
    def BufferedReader stderrReader
    def failOnError = true
    def task
    def FileHelper fileHelper = FileHelper.instance()
    
    abstract def _executeAction()
    
    abstract def validate()
    
    def RemoteAction(Project p) {
        this.project = p
    }
    
    def doIt(Session s) {
        session = s
        _executeAction()
    }
    
    def setStreams(Channel c) {
        stdout = c.inputStream
        stdoutReader = _createReader(stdout)
        if (c.hasProperty("errStream")) {
            stderr = c.errStream
            stderrReader = _createReader(stderr)
        }
    }
    
    def captureOutput(Channel c) {
        def stdErrLine = null
        if (stderr != null) {
            while ((stdErrLine = stderrReader.readLine()) != null) {
                logger.error("<${session.host} stderr> $stdErrLine")
                if (SUDO_PASSWORD_ERROR == stdErrLine) {
                    logger.error("sudo on server ${session.host} rejected password")
                    if (failOnError) throw new RuntimeException("sudo password given was not accepted")
                }
            }
        }
        
        def stdOutLine = null
        while ((stdOutLine = stdoutReader.readLine()) != null) {
            logger.info("<${session.host} stdout> $stdOutLine")
        }
    }
    
    def _createReader(InputStream is) {
        new BufferedReader(new InputStreamReader(is))
    }
}

