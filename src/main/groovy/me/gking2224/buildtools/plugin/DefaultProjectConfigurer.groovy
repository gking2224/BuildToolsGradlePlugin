package me.gking2224.buildtools.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
import org.gradle.api.tasks.wrapper.Wrapper

class DefaultProjectConfigurer extends AbstractProjectConfigurer {
    
    public DefaultProjectConfigurer(Project p) {
        super(p)
    }
    
    def configureProject() {
        
        eclipse()
        
        runId()
        
        project.task("wrapper", type: Wrapper) {
            gradleVersion = '2.14'
        }
        
        resolveValues()
    }
    
    def eclipse() {
        
        if (!project.pluginManager.hasPlugin("eclipse")) project.pluginManager.apply "eclipse"
        project.eclipse {
            project {
                buildCommand 'org.eclipse.jdt.core.javabuilder'
                buildCommand 'org.springframework.ide.eclipse.core.springbuilder'
                natures 'org.eclipse.jdt.core.javanature',
                        'org.springsource.ide.eclipse.gradle.core.nature',
                        'org.springframework.ide.eclipse.core.springnature'
            }
            classpath {
                defaultOutputDir = project.file('build/classes')
                containers = [
                    'org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8',
                    'org.springsource.ide.eclipse.gradle.classpathcontainer'  // Gradle IDE classpath container
                ]
                file {
                    // exclude jar entries from .classpath
                    whenMerged { classpath ->
                        classpath.configure classpath.entries.grep { entry ->
                            !(entry instanceof org.gradle.plugins.ide.eclipse.model.Library)
                        }
                        classpath.entries.findAll {
                            it instanceof org.gradle.plugins.ide.eclipse.model.SourceFolder && it.path.startsWith("src/test/")
                        }*.output = "build/test-classes"
                        classpath.entries.findAll {
                            it instanceof org.gradle.plugins.ide.eclipse.model.SourceFolder && it.path.startsWith("src/integration/")
                        }*.output = "build/integration-classes"
                    }
                }
                downloadSources = true
                downloadJavadoc = true
            }
        }
    }
    
    def runId() {
        if (!project.hasProperty("runId")) {
            project.ext.runId = project.randomString(16)
        }
    }
    
    def resolveValues() {
        project.gradle.taskGraph.beforeTask {Task t->
            if (t != null && t.hasProperty("ext") && DefaultExtraPropertiesExtension.class.isAssignableFrom(t.ext.getClass())) {
                t.ext.getProperties().findAll{k,v->
                    Closure.class.isAssignableFrom(v.getClass())
                }.each {k,v->
                    logger.debug "Auto-resolving closure property $k on task $t.name"
                    t[k] = v()
                }
            }
        }
    }

}
