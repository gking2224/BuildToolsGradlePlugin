package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.GitHelper
import me.gking2224.buildtools.util.Version

import org.eclipse.jgit.api.Status
import org.gradle.api.Plugin
import org.gradle.api.Project

public class BuildToolsGradlePlugin implements Plugin<Project> {

    static final String NAME = "me.gking2224.buildtools"
    static final String GROUP = "Build Tools"
    static final String GRADLE_PROPERTIES_FILE = "gradle.properties"

	void apply(Project project) {
        
        // define tasks
		project.task("buildtoolstest", group:GROUP)
		
		if (!project.hasProperty("mavenRepo")) {
			project.ext.mavenRepo = System.getProperty("user.home")+"/.m2/repository/"
		}
        def url = "file://localhost/${project.mavenRepo}"
		project.uploadArchives {
			repositories {
				mavenDeployer {
					repository(url: url)
				}
			}
		}
		
        createBumpVersionTask(project)
        createForceVersionTask(project)
        createReleaseTasks(project)
        createAssertNoChangesTask(project)
	}
    
    def createAssertNoChangesTask(Project p) {
        p.task("assertNoChanges", group:GROUP) << {
            assertNoChanges(p)
        }
    }
    
    def assertNoChanges(Project p) {
        Status s = GitHelper.instance.getGitStatus(p.rootDir)
        Set<String> mods = s.getModified()
        assertChangesetEmpty("local changes", s.getModified())
        assertChangesetEmpty("local additions", s.getUntracked())
        assertChangesetEmpty("remote changes", s.getAdded())
    }
    
    def assertChangesetEmpty(def type, def paths) {
        
        if (!paths.isEmpty()) {
            println "${paths.size()} ${type}:" 
            paths.each{ println it }
            throw new RuntimeException("Illegal Status: ${type}")
        }
    }
    
    
    def createReleaseTasks(Project p) {
        p.task("release", group:GROUP,
            dependsOn:[
                'test', 'assertNoChanges', 'removeSnapshot', 'commitVersion',
                'uploadArchives', 'bumpVersion']) << {
//            assertNoChanges(p)
//            removeSnapshot(p)
//            commitVersionFile(p)
//            bumpVersion(p)
            commitVersionFile(p)
        }
        
        p.task("removeSnapshot", group:GROUP) << {
            removeSnapshot(p)
        }
    
        p.task("commitVersion", group:GROUP) << {
            commitVersionFile(p)
        }
    }
    
    def commitVersionFile(Project p) {
        
        def fileName = GRADLE_PROPERTIES_FILE
        GitHelper.instance.commitFile(
            p.rootDir, fileName, "RELEASE: removing snapshot")
    }
    
    def createBumpVersionTask(Project p) {
        p.task("bumpVersion", group:GROUP) << {
            bumpVersion(project)
        }
    }
    
    def bumpVersion(Project p) {
        
        def incType = (p.hasProperty("incType"))?
            (p.incType as Version.IncType):
            Version.IncType.PATCH
        def fileName = GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = readProps(p, f)
        
        assert props.version != null
        def v = new Version(props.version)
        def v2 = incrementVersion(v, incType)
        println "Bumping version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        storeProps(props, f)
    }
    
    def createForceVersionTask(Project p) {
        p.task("forceVersion", group:GROUP) << {
            assert project.hasProperty("forcedVersion")
            forceVersion(project.forcedVersion)
        }
    }
    
    def removeSnapshot(Project p) {
        
        def fileName = GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = readProps(p, f)
        def v = new Version(props.version)
        
        assert props.version != null
        def v2 = v.release()
        println "Changing version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        storeProps(props, f)
    }
    
    def forceVersion(def forcedVersion) {
        
        def fileName = GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = readProps(p, f)
        def v = new Version(props.version)
        
        assert props.version != null
        def v2 = new Version(forcedVersion)
        println "Forcing version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        storeProps(props, f)
    }
    
    def readProps(Project p, File f) {
        Properties props = new Properties()
        props.load(f.newDataInputStream())
        return props
    }
    
    def storeProps(Properties p, File f) {
        p.store(f.newWriter(), null)
    }
    
    def incrementVersion(Version v, Version.IncType t) {
        v.increment(t)
    }
}

