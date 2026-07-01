
package com.vali.kmpgenerator

import org.gradle.api.Plugin
import org.gradle.api.Project

/*class KmpArchitectureGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register(
            "createKmpFeature",
            CreateKmpFeatureTask::class.java
        ).configure {
            group = "kmp architecture generator"
            description = "Generates KMP feature files with MVI or MVVM architecture"
        }
    }
}*/

class KmpArchitectureGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create(
            "kmpArchitectureGenerator",
            KmpArchitectureGeneratorExtension::class.java
        )

        project.tasks.register(
            "createKmpFeature",
            CreateKmpFeatureTask::class.java
        ).configure {
            group = "kmp architecture generator"
            description = "Generates KMP feature files with MVI or MVVM architecture"
        }
    }
}