package me.gking2224.buildtools.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status

class GitHelper {

    private static GitHelper instance = new GitHelper()
    
    private GitHelper() {
        
    }
    
    Status getGitStatus(File f) {
        Git git = Git.open(f)
        git.status().call()
    }
    
    def commitFile(File f, String pattern, String message) {
        println(f.absolutePath)
        Git git = Git.open(f)
        
        git.add().addFilepattern(pattern).setUpdate(true);
        git.commit().setMessage(message).
            setAuthor("gking2224", "gking2224@gmail.com").call();
    }
    
    public static void main(String[] args) {
        GitHelper gh = GitHelper.instance
        File f = new File("../AmazonAWSPlugin")
        println gh.getPathsModified(f)
    }
}
