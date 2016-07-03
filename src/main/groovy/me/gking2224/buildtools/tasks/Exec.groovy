package me.gking2224.buildtools.tasks

import org.gradle.api.Project

import com.jcraft.jsch.ChannelExec

class Exec extends RemoteAction {
    def cmd
    def cmds
    def sudo = false
    def sudoUser = "root"
    
    def Exec(Project p) {
        super(p)
    }
    
    @Override
    def validate() {
        assert [cmd, cmds].any {it == null} : "Either 'cmd' or 'cmds' must be set, not both"
        assert ( (cmd != null && [String,GString,Closure].any {it.isAssignableFrom(cmd.class)}) ||
                 (cmds != null && ([Object[], Collection, String].any {it.isAssignableFrom(cmds.class) })) )
        if (cmd != null) {
            cmds = [cmd]
        }
        if (cmds != null && [GString,String].any{it.isAssignableFrom(cmds.class)}) cmds = [cmds]
        cmds = cmds.collect { project.resolveValue(it) }
    }
    
    @Override
    def _executeAction() {
        
        cmds.each {cmd->
            // -S : read password from stdin
            // -k : re-request password every time
            // -p : custom password prompt
            if (sudo) cmd = "sudo -u $sudoUser -S -k -p '${SUDO_PASSWORD_PROMPT}' $cmd"
            def strCommand = "ssh ${session.userName}@${session.host} $cmd"
            logger.info strCommand
            project.dryRunExecute(strCommand, {
                ChannelExec channel
                try {
                    channel = session.openChannel("exec")
                    channel.setCommand(cmd)
                    setStreams(channel)
                    channel.connect()
                    if (sudo && task.remotePassword != null) {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(channel.outputStream))
                        bw.writeLine(task.remotePassword)
                        bw.flush()
                        logger.info "Password written to remote server"
                    }
                    captureOutput(channel)
                    channel.close()
                    def rc = channel.exitStatus
                    if (rc != 0) {
                        logger.error "remote command exit status: $rc"
                        if (failOnError)  throw new RuntimeException("remote command exit status: $rc")
                    }
                }
                finally {
                    logger.debug "Closing exec channel"
                    channel.disconnect()
                }
            })
        }
    }
}