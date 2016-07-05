package me.gking2224.buildtools.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session

class RemoteExec extends DefaultTask {
    
    Logger logger = LoggerFactory.getLogger(RemoteExec.class)
    
    def host
    def remoteUser = System.getProperty("user.name")
    def port = 22
    def keyFileDir = project.fileNameFromParts(System.getProperty('user.home'), ".ssh")
    def keyFileName = "id_rsa"
    def remotePassword = {(project.hasProperty("remotePassword"))?project.remotePassword:""}
    def timeout = 20
    def interval = 5 // the time between retries, in seconds
    
    def _actions = new ArrayList<RemoteAction>()
    def JSch _jsch
    
    def RemoteExec() {
    }
    
    @TaskAction
    def doExec() {
        assert host != null
        println host
        host = project.resolveValue(host)
        println host
        remoteUser = project.resolveValue(remoteUser)
        remotePassword = project.resolveValue(remotePassword)
        ext.getProperties().each {k,v->
            ext[k] = project.resolveValue(ext[k])
        }
        _jsch = new JSch()
        
        def kf = getKeyFile()
        def useKeyFile = (kf != null)
        
        if (kf == null && remotePassword == null)
            throw new IllegalArgumentException("Cannot authenticate - no password or keyfile")
            
        if (kf != null && remotePassword != null)
            logger.warn "Both keyfile and password available - using keyfile (password may still be passed to sudo)"
            
        if (kf != null) {
            logger.debug "Using keyfile $kf"
            _jsch.addIdentity(kf)
        }
        
        Session session = null;
        try {
            session = _jsch.getSession(remoteUser, host, port)
            if (kf == null) {
                logger.debug "Using password"
                session.setPassword(remotePassword)
            }
                        
            project.dryRunExecute("Not creating jsch session", {
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config)
                // timeout doesn't seem to work (may exclude scenarios where the server is not yet on the net)
                // so implementing belt-and-braces
                long startTime = System.currentTimeMillis()
                logger.debug "Connecting to $host with timeout of $timeout secs"
                def keepTrying = true
                Object sema4 = new Object()
                synchronized (sema4) {
                    while (keepTrying) {
                        try {
                            session.connect(timeout *1000)
                            keepTrying = false
                        }
                        catch (JSchException e) {
                            keepTrying = project.withinTimeout(startTime, timeout*1000)
                            logger.info "Caught $e; keepTrying = $keepTrying"
                            if (keepTrying) logger.debug "Will try again in $interval seconds"
                            else throw e
                            sema4.wait(interval*1000)
                        }
                    }
                }
            })
            
            // fail fast
            _actions.each {RemoteAction a->
                a.validate()
            }
            
            _actions.each {RemoteAction a->
                a.doIt(session)
            }
        }
        finally {
            project.dryRunExecute("Not disconnecting jsch session", {
                logger.debug("Closing jsch session")
                session.disconnect()
            })
        }
    }
    
    def getKeyFile() {
        if (keyFileDir == null || keyFileName == null) return null
        def kf = project.fileNameFromParts(keyFileDir, keyFileName)
        if (!project.file(kf).exists()) {
            logger.warn("given keyfile ${kf} does not exist")
            return null
        }
        return kf
    }
    
    def exec(Closure c) {
        def a = new Exec(project)
        _resolveAndAddAction(a, c)
    }
    
    def scp(Closure c) {
        def a = new Scp(project)
        _resolveAndAddAction(a, c)
    }
    
    def _resolveAndAddAction(def a, def c) {
        c.delegate = a
        c()
        a.task = this
        _actions.add(a)
    }
}
