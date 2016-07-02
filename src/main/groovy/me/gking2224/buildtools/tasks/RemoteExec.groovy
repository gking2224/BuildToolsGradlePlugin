package me.gking2224.buildtools.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

class RemoteExec extends DefaultTask {
    
    def host
    def remoteUser = System.getProperty("user.name")
    def port = 22
    def keyFileDirPath = "${System.getProperty('user.home')}/.ssh"
    def keyFileName = "id_rsa"
    def _actions = new ArrayList<Action>()
    def JSch jsch
    
    public RemoteExec() {
        
    }

    
    @TaskAction
    def doExec() {
        assert host != null
        
        def host = project.resolveValue(host)
        def remoteUser = project.resolveValue(remoteUser)
        
        jsch=new JSch()
        
        def kf = getKeyFile()
        jsch.addIdentity(kf)
        
        Session session=jsch.getSession(remoteUser, host, port)
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect()
        _actions.each{Action a->
            a.doIt(session)
        }
        session.disconnect()
    }
    
    def getKeyFile() {
        return "$keyFileDirPath/$keyFileName"
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
        _actions.add(a)
    }
    
    abstract class Action {
        abstract def _executeAction(Session s)
        def Project project
        def BufferedReader reader
        
        def Action(Project p) {
            this.project = p
        }
        def doIt(Session s) {
            
            _executeAction(s)
        }
        
        def setStreams(Channel c) {
            reader = new BufferedReader(new InputStreamReader(c.getInputStream()))
//            c.setInputStream(System.in);
//            c.setOutputStream(System.out);
        }
    }
    
    class Scp extends Action {
        def fromDir
        def toDir
        def file
        
        def Scp(Project p) {
            super(p)
        }
        @Override
        def _executeAction(Session s) {
            fromDir = project.resolveValue(fromDir)
            file = project.resolveValue(file)
            toDir = project.resolveValue(toDir)
            file = resolveFile()
            project.info "scp -P ${s.port} $file ${s.userName}@${s.host}:${toDir}"
            ChannelSftp channel = s.openChannel("sftp")
            setStreams(channel)
            channel.connect()
            channel.put(file, toDir)
            channel.outputStream.flush()
            channel.inputStream.close()
            channel.outputStream.close()
            channel.close()
        }
        
        def resolveFile() {
            def f = "${fromDir}/${file}"
            def File ff = project.file(f)
            assert ff.exists()
            f
        }
    }
    
    class Exec extends Action {
        def cmd
        def BufferedReader errReader
        
        def Exec(Project p) {
            super(p)
        }
        
        @Override
        def _executeAction(Session s) {
            assert cmd != null
            def c
            if (!([Object[], Collection].any {it.isAssignableFrom(cmd.class) } )) {
                c = [cmd]
            }
            def cmds = cmd.collect { project.resolveValue(it) }
            
            cmds.each {cc->
                ChannelExec channel = s.openChannel("exec")
                project.info "ssh ${s.userName}@${s.host} $cc"
                channel.setCommand(cc + "\n")
                setStreams(channel)
                errReader = new BufferedReader(new InputStreamReader(channel.getErrStream()))
                channel.connect()
                
                def outputLine = null
                while ((outputLine = reader.readLine()) != null) {
                    project.info("<${s.host} stdout> $outputLine")
                }
                outputLine = null
                while ((outputLine = errReader.readLine()) != null) {
                    project.error("<${s.host} stderr> $outputLine")
                }
                channel.disconnect()
            }
        }
    }
}
