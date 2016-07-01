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
        
        def h = project.resolveValue(host)
        def u = project.resolveValue(remoteUser)
        
        jsch=new JSch()
        
        def kf = getKeyFile()
        jsch.addIdentity(kf)
        
        Session session=jsch.getSession(u, h, port)
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
        
        def Action(Project p) {
            this.project = p
        }
        def doIt(Session s) {
            _executeAction(s)
        }
        
        def setStreams(Channel c) {
            c.setInputStream(new java.io.InputStream() {
                @Override
                public int read() {}
            });
            c.setOutputStream(new java.io.OutputStream() {
                public void write(int b) {}
            });
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
            def f = resolveFile()
            ChannelSftp channel = s.openChannel("sftp")
            channel.connect()
            channel.put(f, toDir)
            channel.close()
            project.info "scp -P ${s.port} $f ${s.userName}@${s.host}:${toDir}"
        }
        
        def resolveFile() {
            def f = "$fromDir/$file"
            def File ff = project.file(f)
            assert ff.exists()
            f
        }
    }
    
    class Exec extends Action {
        def cmd
        
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
            ChannelExec channel = s.openChannel("exec")
            setStreams(channel)
            channel.setErrStream(System.err)
            channel.connect()
            cmds.each {cc->
                project.info "ssh ${s.userName}@${s.host} $cc"
                channel.setCommand(cc)
                channel.start()
            }
            channel.disconnect()
        }
    }
}
