package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.tasks.GitCommit
import me.gking2224.buildtools.util.GitHelper
import me.gking2224.buildtools.util.Version

import org.eclipse.jgit.api.Status
import org.gradle.api.Project

class ReleaseTasksConfigurer extends AbstractProjectConfigurer {
    
    def ReleaseTasksConfigurer(Project p) {
        super(p)
    }
    
    def configureProject() {
        
        createBumpVersionTask()
        createForceVersionTask()
        createAssertNoChangesTask()
        createReleaseTasks()
    }
    
    def createAssertNoChangesTask() {
        project.task("assertNoChanges", group:BuildToolsGradlePlugin.GROUP) << {
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
            project.info "${paths.size()} $type"
            paths.each{ project.info it }
            throw new RuntimeException("Illegal Status: ${type}")
        }
    }
    
    def createReleaseTasks() {
        project.gradle.taskGraph.whenReady {taskGraph ->
            def url = (taskGraph.hasTask(project.tasks.release))?"artifactory.release.url":"artifactory.snapshot.url"
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: project[url]) {
                            authentication(userName: project["artifactory.username"], password: project["artifactory.password"])
                        }
                    }
                }
            }
        }
        
        project.task("release", group:BuildToolsGradlePlugin.GROUP,
            dependsOn:[
                'check', 'assertNoChanges', 'removeSnapshot', 'uploadArchives'])
        
        project.task("removeSnapshot", type:GitCommit, group:BuildToolsGradlePlugin.GROUP) {
            pattern = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
            message = "RELEASE: removing snapshot"
        }
        project.tasks.removeSnapshot.doFirst {
            removeSnapshot()
        }
        project.tasks.assertNoChanges.mustRunAfter "check"
        project.tasks.removeSnapshot.mustRunAfter "assertNoChanges"
        project.tasks.uploadArchives.mustRunAfter "removeSnapshot"
    }
    
    def commitVersionFile() {
        
        def fileName = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
        GitHelper.getInstance(project).commitFile(
            project.rootDir, fileName, )
    }
    
    def createBumpVersionTask() {
        
        project.task("postRelease", group:BuildToolsGradlePlugin.GROUP,
            dependsOn:[
                'bumpVersion', 'uploadArchives'])
        project.tasks.uploadArchives.mustRunAfter "bumpVersion"
        
        project.task("bumpVersion", group:BuildToolsGradlePlugin.GROUP, type:GitCommit) {
            pattern = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
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
        def fileName = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = project.readProps(f)
        
        assert props.version != null
        def v = new Version(props.version)
        def v2 = v.increment(incType)
        project.info "Bumping version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        project.storeProps(props, f)
        project.version = v2.rawVersion
    }
    
    def createForceVersionTask() {
        project.task("forceVersion", group:BuildToolsGradlePlugin.GROUP) << {
            assert project.hasProperty("forcedVersion")
            forceVersion(project.forcedVersion)
        }
    }
    
    def removeSnapshot() {
        
        def fileName = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = project.readProps(f)
        def v = new Version(props.version)
        
        assert props.version != null
        def v2 = v.release()
        project.info "Changing version from ${v} to ${v2}"
        props.version = v2.rawVersion
        project.storeProps(props, f)
        project.version = v2.rawVersion
    }
    
    def forceVersion(def forcedVersion) {
        
        def fileName = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
        def f = new File(fileName)
        def props = project.readProps(f)
        def v = new Version(props.version)
        
        assert props.version != null
        def v2 = new Version(forcedVersion)
        project.info "Forcing version from ${v} to ${v2}"
        
        props.version = v2.rawVersion
        project.storeProps(props, f)
        project.version = v2.rawVersion
    }
}
