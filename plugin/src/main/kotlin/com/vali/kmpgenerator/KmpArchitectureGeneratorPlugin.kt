
package com.vali.kmpgenerator

import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpArchitectureGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "kmpArchitectureGenerator",
            KmpArchitectureGeneratorExtension::class.java
        )

        val createKmpFeatureTask = project.tasks.register(
            "createKmpFeature",
            CreateKmpFeatureTask::class.java
        ) {
            group = "kmp architecture generator"
            description = "Generates KMP feature files with MVI or MVVM architecture"

            projectDirectory.set(project.layout.projectDirectory)
        }

        project.afterEvaluate {
            createKmpFeatureTask.configure {
                defaultPackageName.set(extension.packageName)
                defaultOutputDir.set(extension.outputDir)
                defaultArchitecture.set(extension.defaultArchitecture)
                defaultDi.set(extension.defaultDi)
                defaultNetworking.set(extension.defaultNetworking)
            }
        }
    }
}