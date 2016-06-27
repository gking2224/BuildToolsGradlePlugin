package me.gking2224.util.git

import static org.junit.Assert.*

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.Test

class JGitTest {

//    @Test
    void test() {
        File f = new File("../AmazonAWSPlugin")
        Git git = Git.open(f)
        try {
            Status status = git.status().call()
            println status.getModified()
        }
        finally {
        }
    }
}
