package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.tasks.GitCommit
import me.gking2224.buildtools.util.GitHelper
import me.gking2224.buildtools.util.PropertiesResolver;
import me.gking2224.buildtools.util.Version

import org.eclipse.jgit.api.Status
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.wrapper.Wrapper

public class BuildToolsGradlePlugin implements Plugin<Project> {

    static final String NAME = "me.gking2224.buildtools"
    static final String GROUP = "Build Tools"
    static final String GRADLE_PROPERTIES_FILE = "gradle.properties"
    static final String SECRET_PROPERTIES_FILE = "secret.properties"
    
    def Project project

	void apply(Project p) {
        this.project = p
        // declare envs extension
        project.extensions.create(
            EnvironmentsHandler.KEY,
             EnvironmentsHandler, project)
        
        
        if (project.file(SECRET_PROPERTIES_FILE).exists()) {
            new PropertiesResolver(p).resolveProperties(readProps(project.file(SECRET_PROPERTIES_FILE)))
        }
		if (!project.hasProperty("mavenRepo")) {
			project.ext.mavenRepo = System.getProperty("user.home")+"/.m2/repository/"
		}
        def url = "file://localhost/${project.mavenRepo}"
		project.install {
			repositories {
				mavenDeployer {
					repository(url: url)
				}
			}
		}
        println "uploadArchives not configured"
//        project.uploadArchives {
//            repositories {
//                mavenDeployer {
////                    repository(url: (project.gradle.startParameter.taskNames.contains("release"))?System.getProperty("artifactory.release.url"):System.getProperty("artifactory.snapshot.url")) {
////                        authentication(userName: System.getProperty("artifactory.username"), password: System.getProperty("artifactory.password"))
////                    }
////                    repository(url: System.getProperty("artifactory.release.url")) {
////                        authentication(userName: System.getProperty("artifactory.username"), password: System.getProperty("artifactory.password"))
////                    }
//                    repository(url: System.getProperty("artifactory.snapshot.url")) {
//                        authentication(userName: System.getProperty("artifactory.username"), password: System.getProperty("artifactory.password"))
//                    }
//                }
//            }
//        }
        
        if (project.file("src/integration").exists()) {
            configureIntegrationTests()
        }
		
        createBumpVersionTask()
        createForceVersionTask()
        createAssertNoChangesTask()
        createReleaseTasks()
        
        project.ext.isDryRun = {
            if (!project.hasProperty("dryRun")) return false
            def dr = project.dryRun.toLowerCase()
            return ["true", "t", "", "yes", "y"].contains(dr)
        }
        project.ext.notRunning = {m ->
            println "DryRun - not running{$m}"
        }
        project.task("wrapper", type: Wrapper) {
            gradleVersion = '2.7'
        }
        project.ext.resolve = {a ->
            if (a == null) return null
            else if (a instanceof Closure) return a()
            else return a
        }
	}
    
    
    def createAssertNoChangesTask() {
        project.task("assertNoChanges", group:GROUP) << {
            assertNoChanges()
        }
    }
    
    def assertNoChanges() {
        Status s = GitHelper.getInstance(project).getGitStatus(project.rootDir)
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
        project.gradle.taskGraph.whenReady {taskGraph ->
            if (taskGraph.hasTask("release")) {
                
            }
        }
        
        project.task("release", group:GROUP,
            dependsOn:[
                'check', 'assertNoChanges', 'removeSnapshot'])
              
        
        project.task("removeSnapshot", type:GitCommit, group:GROUP) {
            pattern = GRADLE_PROPERTIES_FILE
            message = "RELEASE: removing snapshot"
        }
        project.tasks.removeSnapshot.doFirst {
            removeSnapshot()
        }
        project.tasks.assertNoChanges.mustRunAfter "check"
        project.tasks.removeSnapshot.mustRunAfter "assertNoChanges"
    }
    
    def commitVersionFile() {
        
        def fileName = GRADLE_PROPERTIES_FILE
        GitHelper.getInstance(project).commitFile(
            project.rootDir, fileName, )
    }
    
    def createBumpVersionTask() {
        project.task("bumpVersion", group:GROUP, type:GitCommit) {
            pattern = GRADLE_PROPERTIES_FILE
            message = "RELEASE: increasing version"
        }
        project.tasks.bumpVersion.doFirst {
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
        def v2 = v.increment(incType)
        println "Bumping version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        storeProps(props, f)
        project.version = v2.rawVersion
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
        project.version = v2.rawVersion
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
        project.version = v2.rawVersion
    }
    
    def readProps(File f) {
        Properties props = new Properties()
        props.load(f.newDataInputStream())
        return props
    }
    
    def storeProps(Properties p, File f) {
        if (project.isDryRun()) project.notRunning("StoreProps ($p) to file ${f.absolutePath}")
        else p.store(f.newWriter(), null)
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

