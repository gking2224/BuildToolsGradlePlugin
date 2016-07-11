package me.gking2224.buildtools.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class IntegrationTestConfigurer extends AbstractProjectConfigurer {
    
    def IntegrationTestConfigurer(Project p) {
        super(p)
    }
    
    def configureProject() {
        
        project.task("checkIntegration", group: "Verification tasks")
        if (project.file("src/integration").exists()) {
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
                classpath = project.sourceSets.integration.runtimeClasspath + project.sourceSets.main.output + project.sourceSets.test.output
            }
            project.tasks.withType(Test) {task->
                task.reports.html.destination = project.file("${project.reporting.baseDir}/${name}")
            }
            
            project.tasks.checkIntegration.dependsOn("integrationTest")
        }
        
    }
}
