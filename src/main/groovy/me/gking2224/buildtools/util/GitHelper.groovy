package me.gking2224.buildtools.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.dircache.DirCacheEntry
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.HttpTransport
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.util.FS

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session

class GitHelper {

    def sshSessionFactory
    def transportConfigCallback
    def credentialsProvider
    def static GitHelper instance

    def GitHelper(def username, def password) {
        credentialsProvider = new UsernamePasswordCredentialsProvider(username, password)
        sshSessionFactory = new JschConfigSessionFactory() {
                    @Override
                    protected void configure( Host host, Session session ) {
                        // do nothing
                    }
                    @Override
                    protected JSch createDefaultJSch( FS fs ) throws JSchException {
                        JSch defaultJSch = super.createDefaultJSch( fs );
                        def key = System.getProperty("git.build.private.key")
                        println "GitHelper: using key $key"
                        if (key != null) defaultJSch.addIdentity( key )
                        return defaultJSch
                    }
                };
        transportConfigCallback = new TransportConfigCallback() {
                    @Override
                    public void configure( Transport transport ) {
                        if (SshTransport.class.isAssignableFrom(transport.getClass())) {
                            SshTransport sshTransport = ( SshTransport )transport
                            sshTransport.setSshSessionFactory( sshSessionFactory )
                        }
                        else if (HttpTransport.class.isAssignableFrom(transport.getClass())) {
                            HttpTransport httpTransport = (HttpTransport)transport
                            httpTransport.setCredentialsProvider(credentialsProvider)
                        }
                    }
                };
    }
    
    def static getInstance(def project) {
        if (instance == null) instance = new GitHelper(project["git.username"], project["git.password"])
        return instance
    }

    def Status getGitStatus(File f) {
        Git git = Git.open(f)
        git.status().call()
    }
    
    def getLastCommitMessage(File f) {
        Git git = Git.open(f)
        RevCommit rc = git.log().setMaxCount(1).call().iterator().next()
        return rc.getShortMessage()
    }

    def commitFile(File f, String pattern, String message) {
        Git git = Git.open(f)
        DirCache dc = git.add().addFilepattern(pattern).setUpdate(true).call()
        dc.getEntriesWithin(pattern).each{DirCacheEntry e-> println e.getPathString()}
        RevCommit rc  = git.commit().setMessage(message).call()
        git.push().setTransportConfigCallback(transportConfigCallback).
            setOutputStream(System.out).call()
    }
}
