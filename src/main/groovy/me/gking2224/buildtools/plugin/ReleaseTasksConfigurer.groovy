package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.tasks.GitCommit
import me.gking2224.buildtools.util.GitHelper
import me.gking2224.buildtools.util.Version

import org.eclipse.jgit.api.Status
import org.gradle.api.Project

import org.gradle.api.tasks.bundling.Jar

class ReleaseTasksConfigurer extends AbstractProjectConfigurer {

    static final String RELEASE_COMMIT_MESSAGE = "RELEASE: removing snapshot"
    static final String BUMP_VERSION_COMMIT_MESSAGE = "RELEASE: increasing version"
    
    def ReleaseTasksConfigurer(Project p) {
        super(p)
    }
    
    def configureProject() {
        
        createBumpVersionTask()
        createForceVersionTask()
        createAssertNoChangesTask()
        
        if (project.hasProperty("publish.repository.release.url") && project.hasProperty("publish.repository.snapshot.url")) {
            createReleaseTasks()
        }
        
        clientLibs()
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
    
    def previousCommitIsRelease() {
        String msg = GitHelper.getInstance(project).getLastCommitMessage(project.rootDir)
        return RELEASE_COMMIT_MESSAGE.equals(msg) || BUMP_VERSION_COMMIT_MESSAGE.equals(msg)
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
            def url = (taskGraph.hasTask(project.tasks.release))?"publish.repository.release.url":"publish.repository.snapshot.url"
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: project[url]) {
                            authentication(userName: project["publish.repository.username"], password: project["publish.repository.password"])
                        }
                    }
                }
            }
        }
        
        project.task("release", group:BuildToolsGradlePlugin.GROUP)
        
        if (!previousCommitIsRelease()) {
            project.tasks.release.dependsOn(['check', 'assertNoChanges', 'commitReleaseVersion', 'uploadArchives'])
        }
        
        project.task("forceRelease", group:BuildToolsGradlePlugin.GROUP)
        project.tasks.forceRelease.dependsOn(['check', 'assertNoChanges', 'commitReleaseVersion', 'uploadArchives'])
        
        project.task("removeSnapshot", group: BuildToolsGradlePlugin.GROUP) << {
            removeSnapshot()
        }
        
        project.task("commitReleaseVersion", type:GitCommit, group:BuildToolsGradlePlugin.GROUP) {
            pattern = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
            message = RELEASE_COMMIT_MESSAGE
        }
        project.tasks.commitReleaseVersion.dependsOn 'removeSnapshot'
        
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
            dependsOn: ['commitNewVersion', 'uploadArchives'])
        project.tasks.uploadArchives.mustRunAfter "commitNewVersion"
        
        project.task("commitNewVersion", group:BuildToolsGradlePlugin.GROUP, type:GitCommit) {
            pattern = BuildToolsGradlePlugin.GRADLE_PROPERTIES_FILE
            message = BUMP_VERSION_COMMIT_MESSAGE
        }
        
        project.task("bumpVersion", group:BuildToolsGradlePlugin.GROUP) << {
            bumpVersion()
        }
        project.tasks.commitNewVersion.dependsOn 'bumpVersion'
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
    
    def clientLibs() {
        project.configurations {
            clientlib
        }
        
        project.sourceSets {
            clientlib {
                java {
                    srcDir project.file("src/main/java")
                    include '**/client/**'
                    compileClasspath = project.configurations.clientlib
                }
            }
        }
        
        project.task("clientlib", type: Jar) {
            baseName = project.name+'-client'
            classifier = 'client'
            from project.sourceSets.clientlib.output
        }
        
        
        project.artifacts {
            archives project.tasks.clientlib
        }
        
        project.install {
            repositories {
                mavenDeployer {
                    def clientPom = addFilter('client') {artifact, file ->
                        artifact.extraAttributes.classifier == 'client'
                    }
                    clientPom.whenConfigured {pom->
                        def deps = []
                        project.configurations.clientlib.dependencies.each {deps << it.name}
                        pom.setDependencies(pom.getDependencies().findAll {d->
                            deps.contains(d.artifactId)
                        })
                    }
                }
            }
        }
        
    }
}
