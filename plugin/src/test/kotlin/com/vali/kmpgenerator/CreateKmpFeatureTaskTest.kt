package com.vali.kmpgenerator


import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import kotlin.io.path.createTempDirectory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse


class CreateKmpFeatureTaskTest {

    @kotlin.test.Test
    fun `create MVI feature generates expected files`() {
        val projectDir = createTempDirectory().toFile()

        createTestProject(projectDir)

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":sample:createKmpFeature",
                "--name",
                "Profile",
                "--architecture",
                "mvi",
                "--packageName",
                "com.vali.shared"
            )
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":sample:createKmpFeature")?.outcome
        )

        val featureDir = projectDir.resolve(
            "sample/src/commonMain/kotlin/com/vali/shared/features/profile"
        )

        assertTrue(featureDir.resolve("presentation/ProfileState.kt").exists())
        assertTrue(featureDir.resolve("presentation/ProfileAction.kt").exists())
        assertTrue(featureDir.resolve("presentation/ProfileResult.kt").exists())
        assertTrue(featureDir.resolve("presentation/ProfileEffect.kt").exists())
        assertTrue(featureDir.resolve("presentation/ProfileReducer.kt").exists())
        assertTrue(featureDir.resolve("presentation/ProfileViewModel.kt").exists())

        assertTrue(featureDir.resolve("domain/ProfileRepository.kt").exists())
        assertTrue(featureDir.resolve("domain/ProfileUseCase.kt").exists())

        assertTrue(featureDir.resolve("data/ProfileRemoteDataSource.kt").exists())
        assertTrue(featureDir.resolve("data/ProfileRepositoryImpl.kt").exists())

        assertTrue(featureDir.resolve("di/ProfileModule.kt").exists())
        assertTrue(featureDir.resolve(".kmp-feature-generator").exists())
    }

    @Test
    fun `create MVVM feature generates expected files`() {
        val projectDir = createTempDirectory().toFile()

        createTestProject(projectDir)

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":sample:createKmpFeature",
                "--name",
                "Settings",
                "--architecture",
                "mvvm",
                "--packageName",
                "com.vali.shared"
            )
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":sample:createKmpFeature")?.outcome
        )

        val featureDir = projectDir.resolve(
            "sample/src/commonMain/kotlin/com/vali/shared/features/settings"
        )

        assertTrue(featureDir.resolve("presentation/SettingsUiState.kt").exists())
        assertTrue(featureDir.resolve("presentation/SettingsViewModel.kt").exists())

        assertTrue(!featureDir.resolve("presentation/SettingsAction.kt").exists())
        assertTrue(!featureDir.resolve("presentation/SettingsReducer.kt").exists())
    }

    @Test
    fun `create Ktor feature generates HttpClient remote data source`() {
        val projectDir = createTempDirectory().toFile()

        createTestProject(projectDir)

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":sample:createKmpFeature",
                "--name",
                "VacancySearch",
                "--architecture",
                "mvi",
                "--di",
                "koin",
                "--networking",
                "ktor",
                "--packageName",
                "com.vali.shared"
            )
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":sample:createKmpFeature")?.outcome
        )

        val featureDir = projectDir.resolve(
            "sample/src/commonMain/kotlin/com/vali/shared/features/vacancysearch"
        )

        val remoteDataSource = featureDir
            .resolve("data/VacancySearchRemoteDataSource.kt")
            .readText()

        assertTrue(remoteDataSource.contains("import io.ktor.client.HttpClient"))
        assertTrue(remoteDataSource.contains("private val httpClient: HttpClient"))

        val module = featureDir
            .resolve("di/VacancySearchModule.kt")
            .readText()

        assertTrue(module.contains("single { VacancySearchRemoteDataSource(get()) }"))

        val networkDir = projectDir.resolve(
            "sample/src/commonMain/kotlin/com/vali/shared/core/network"
        )

        assertTrue(networkDir.resolve("HttpClientFactory.kt").exists())
        assertTrue(networkDir.resolve("NetworkModule.kt").exists())
    }

    private fun createTestProject(projectDir: java.io.File) {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
                pluginManagement {
                    repositories {
                        google()
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }

                dependencyResolutionManagement {
                    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

                    repositories {
                        google()
                        mavenCentral()
                    }
                }

                rootProject.name = "test-project"

                include(":sample")
            """.trimIndent()
        )

        projectDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    // root project
                }
            """.trimIndent()
        )

        val sampleDir = projectDir.resolve("sample")
        sampleDir.mkdirs()

        sampleDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    kotlin("multiplatform") version "2.2.21"
                    id("com.vali.kmp-architecture-generator")
                }

                kotlin {
                    jvm()

                    sourceSets {
                        commonMain {
                            dependencies {
                                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                                implementation("io.insert-koin:koin-core:4.2.0")
                                implementation("io.ktor:ktor-client-core:3.3.0")
                                implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
                                implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
                                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                            }
                        }
                    }
                }

                kmpArchitectureGenerator {
                    packageName = "com.vali.shared"
                    outputDir = "src/commonMain/kotlin"
                    defaultArchitecture = "mvi"
                    defaultDi = "none"
                    defaultNetworking = "none"
                }
            """.trimIndent()
        )
    }
    @kotlin.test.Test
    fun `changing architecture without recreate fails`(): Unit {
        val projectDir = createTempDirectory().toFile()

        createTestProject(projectDir)

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":sample:createKmpFeature",
                "--name",
                "Profile",
                "--architecture",
                "mvi",
                "--packageName",
                "com.vali.shared"
            )
            .withPluginClasspath()
            .build()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":sample:createKmpFeature",
                "--name",
                "Profile",
                "--architecture",
                "mvvm",
                "--packageName",
                "com.vali.shared"
            )
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(
            result.output.contains("Feature 'Profile' already exists with architecture 'MVI'")
        )

        assertTrue(
            result.output.contains("Use --recreate to recreate it")
        )
    }

    @Test
    fun `changing architecture with recreate deletes old files and generates new files`() {
        val projectDir = createTempDirectory().toFile()

        createTestProject(projectDir)

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":sample:createKmpFeature",
                "--name",
                "Profile",
                "--architecture",
                "mvi",
                "--packageName",
                "com.vali.shared"
            )
            .withPluginClasspath()
            .build()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":sample:createKmpFeature",
                "--name",
                "Profile",
                "--architecture",
                "mvvm",
                "--packageName",
                "com.vali.shared",
                "--recreate"
            )
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":sample:createKmpFeature")?.outcome
        )

        val featureDir = projectDir.resolve(
            "sample/src/commonMain/kotlin/com/vali/shared/features/profile"
        )

        assertTrue(featureDir.resolve("presentation/ProfileUiState.kt").exists())
        assertTrue(featureDir.resolve("presentation/ProfileViewModel.kt").exists())

        assertFalse(featureDir.resolve("presentation/ProfileAction.kt").exists())
        assertFalse(featureDir.resolve("presentation/ProfileResult.kt").exists())
        assertFalse(featureDir.resolve("presentation/ProfileEffect.kt").exists())
        assertFalse(featureDir.resolve("presentation/ProfileReducer.kt").exists())

        val markerFile = featureDir.resolve(".kmp-feature-generator")
        assertTrue(markerFile.exists())
        assertTrue(markerFile.readText().contains("architecture=MVVM"))
    }
}

