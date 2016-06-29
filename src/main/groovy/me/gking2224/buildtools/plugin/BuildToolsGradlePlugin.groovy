package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.GitHelper
import me.gking2224.buildtools.util.Version

import org.eclipse.jgit.api.Status
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

public class BuildToolsGradlePlugin implements Plugin<Project> {

    static final String NAME = "me.gking2224.buildtools"
    static final String GROUP = "Build Tools"
    static final String GRADLE_PROPERTIES_FILE = "gradle.properties"
    
    def Project project

	void apply(Project p) {
        this.project = p
        // declare envs extension
        project.extensions.create(
            EnvironmentsHandler.KEY,
             EnvironmentsHandler, project)
        
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
        
        if (project.file("src/integration").exists()) {
            configureIntegrationTests()
        }
		
        createBumpVersionTask()
        createForceVersionTask()
        createReleaseTasks()
        createAssertNoChangesTask()
	}
    
    def createAssertNoChangesTask() {
        project.task("assertNoChanges", group:GROUP) << {
            assertNoChanges()
        }
    }
    
    def assertNoChanges() {
        Status s = GitHelper.instance.getGitStatus(project.rootDir)
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
    
    
    def createReleaseTasks() {
        project.task("release", group:GROUP,
            dependsOn:[
                'test', 'assertNoChanges', 'removeSnapshot', 'commitVersion',
                'uploadArchives', 'bumpVersion']) << {
//            assertNoChanges()
//            removeSnapshot()
//            commitVersionFile()
//            bumpVersion()
            commitVersionFile()
        }
        
        project.task("removeSnapshot", group:GROUP) << {
            removeSnapshot()
        }
    
        project.task("commitVersion", group:GROUP) << {
            commitVersionFile()
        }
    }
    
    def commitVersionFile() {
        
        def fileName = GRADLE_PROPERTIES_FILE
        GitHelper.instance.commitFile(
            project.rootDir, fileName, "RELEASE: removing snapshot")
    }
    
    def createBumpVersionTask() {
        project.task("bumpVersion", group:GROUP) << {
            bumpVersion()
        }
    }
    
    def bumpVersion() {
        
        def incType = (project.hasProperty("incType"))?
            (project.incType as Version.IncType):
            Version.IncType.PATCH
        def fileName = GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = readProps(f)
        
        assert props.version != null
        def v = new Version(props.version)
        def v2 = incrementVersion(v, incType)
        println "Bumping version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        storeProps(props, f)
    }
    
    def createForceVersionTask() {
        project.task("forceVersion", group:GROUP) << {
            assert project.hasProperty("forcedVersion")
            forceVersion(project.forcedVersion)
        }
    }
    
    def removeSnapshot() {
        
        def fileName = GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = readProps(f)
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
        def props = readProps(f)
        def v = new Version(props.version)
        
        assert props.version != null
        def v2 = new Version(forcedVersion)
        println "Forcing version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        storeProps(props, f)
    }
    
    def readProps(File f) {
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
    
    def configureIntegrationTests() {
        
        project.sourceSets {
            integration {
                java {
                    srcDir project.file("src/integration/java")
                    compileClasspath += main.output + test.output
                }
                groovy {
                    srcDir project.file("src/integration/groovy")
                    compileClasspath += main.output + test.output
                }
                resources.srcDir project.file("src/integration/resources")
                output.classesDir = "build/integration-classes"
            }
            test.output.classesDir = "build/test-classes"
            main.output.classesDir = "build/classes"
        }
        
        project.configurations {
            integrationCompile.extendsFrom testCompile
            integrationRuntime.extendsFrom testRuntime
        }
        
        project.task("integrationTest", group:"Verification tasks", type:Test, dependsOn:['integrationClasses']) {
            testClassesDir = project.sourceSets.integration.output.classesDir
            classpath = project.sourceSets.integration.runtimeClasspath
        }
        project.tasks.withType(Test) {task->
            task.reports.html.destination = project.file("${project.reporting.baseDir}/${name}")
        }
        
        
//        project.tasks.check.dependsOn "integrationTest"
    }
}

