# KMP Architecture Generator

Gradle plugin for generating Kotlin Multiplatform feature modules with MVI or MVVM architecture.

The plugin helps quickly create a clean feature-first structure for KMP projects with optional Koin DI and Ktor networking support.

## Features

- Generate Kotlin Multiplatform feature modules
- Choose architecture: MVI or MVVM
- Generate Clean Architecture layers:
  - data
  - domain
  - presentation
  - di
- Optional Koin DI module generation
- Optional Ktor RemoteDataSource generation
- Optional shared HttpClientFactory and NetworkModule generation
- Safe architecture protection with marker files
- Recreate existing generated feature with `--recreate`
- Gradle configuration cache support
- External usage via JitPack
- Gradle TestKit coverage

## Installation via JitPack

The plugin can be used as an external Gradle plugin through JitPack.

Add JitPack to `pluginManagement` in the root `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.vali.kmp-architecture-generator") {
                useModule(
                    "com.github.RobValiew.kmp-architecture-generator:kmp-architecture-generator-plugin:${requested.version}"
                )
            }
        }
    }
}
```

Apply the plugin in your KMP module:

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.vali.kmp-architecture-generator") version "v1.0.1"
}
```

Then configure the generator:

```kotlin
kmpArchitectureGenerator {
    packageName = "com.example.shared"
    outputDir = "src/commonMain/kotlin"
    defaultArchitecture = "mvi"
    defaultDi = "none"
    defaultNetworking = "none"
}
```

## Local development setup

For local development, include the plugin build in the root `settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("plugin")

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

rootProject.name = "kmp-architecture-generator"

include(":sample")
```

Apply the plugin in your KMP module:

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.vali.kmp-architecture-generator")
}
```

## DSL configuration

```kotlin
kmpArchitectureGenerator {
    packageName = "com.vali.shared"
    outputDir = "src/commonMain/kotlin"
    defaultArchitecture = "mvi"
    defaultDi = "none"
    defaultNetworking = "none"
}
```

## Usage

Generate an MVI feature:

```bash
./gradlew :sample:createKmpFeature --name Profile --architecture mvi
```

Generate an MVVM feature:

```bash
./gradlew :sample:createKmpFeature --name Settings --architecture mvvm
```

Generate an MVI feature with Koin:

```bash
./gradlew :sample:createKmpFeature --name Auth --architecture mvi --di koin
```

Generate a feature with Koin and Ktor:

```bash
./gradlew :sample:createKmpFeature --name VacancySearch --architecture mvi --di koin --networking ktor
```

Recreate an existing generated feature:

```bash
./gradlew :sample:createKmpFeature --name Profile --architecture mvvm --recreate
```

## Generated MVI structure

```text
features/profile/
 ├── presentation/
 │   ├── ProfileState.kt
 │   ├── ProfileAction.kt
 │   ├── ProfileResult.kt
 │   ├── ProfileEffect.kt
 │   ├── ProfileReducer.kt
 │   └── ProfileViewModel.kt
 ├── domain/
 │   ├── ProfileRepository.kt
 │   └── ProfileUseCase.kt
 ├── data/
 │   ├── ProfileRemoteDataSource.kt
 │   └── ProfileRepositoryImpl.kt
 └── di/
     └── ProfileModule.kt
```

## Generated MVVM structure

```text
features/settings/
 ├── presentation/
 │   ├── SettingsUiState.kt
 │   └── SettingsViewModel.kt
 ├── domain/
 │   ├── SettingsRepository.kt
 │   └── SettingsUseCase.kt
 ├── data/
 │   ├── SettingsRemoteDataSource.kt
 │   └── SettingsRepositoryImpl.kt
 └── di/
     └── SettingsModule.kt
```

## Generated packages

If the DSL is configured like this:

```kotlin
kmpArchitectureGenerator {
    packageName = "com.vali.shared"
}
```

Then a feature named `Profile` will be generated under:

```text
src/commonMain/kotlin/com/vali/shared/features/profile/
```

With packages like:

```text
package com.vali.shared.features.profile.presentation
package com.vali.shared.features.profile.domain
package com.vali.shared.features.profile.data
package com.vali.shared.features.profile.di
```

## Feature-first structure

The generator uses a feature-first package structure.

Each generated feature contains its own layers:

```text
features/map/
 ├── data/
 ├── domain/
 ├── presentation/
 └── di/

features/tasks/
 ├── data/
 ├── domain/
 ├── presentation/
 └── di/
```

This does not create a separate Gradle module for each feature.

All generated features are created inside the selected KMP module, usually `sharedLogic` or `shared`.

This structure keeps all code related to one feature close together and is useful for medium and large KMP projects.

## Architecture protection

The plugin creates a marker file inside each generated feature:

```text
.kmp-feature-generator
```

Example:

```text
name=Profile
architecture=MVI
di=KOIN
networking=KTOR
```

If a feature was generated as MVI and you try to generate it again as MVVM, the plugin stops with an error.

Example:

```bash
./gradlew :sample:createKmpFeature --name Profile --architecture mvvm
```

To intentionally recreate the feature, use:

```bash
./gradlew :sample:createKmpFeature --name Profile --architecture mvvm --recreate
```

## Koin support

When `--di koin` is used, the plugin generates a Koin module.

Example for MVI:

```text
package com.vali.shared.features.auth.di

import com.vali.shared.features.auth.data.AuthRemoteDataSource
import com.vali.shared.features.auth.data.AuthRepositoryImpl
import com.vali.shared.features.auth.domain.AuthRepository
import com.vali.shared.features.auth.domain.AuthUseCase
import com.vali.shared.features.auth.presentation.AuthReducer
import com.vali.shared.features.auth.presentation.AuthViewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthRemoteDataSource() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { AuthUseCase(get()) }
    factory { AuthReducer() }
    factory { AuthViewModel(get(), get()) }
}
```

When Ktor networking is enabled, the RemoteDataSource receives `HttpClient` from Koin:

```kotlin
single { AuthRemoteDataSource(get()) }
```

## Ktor support

When `--networking ktor` is used, the plugin generates a RemoteDataSource with `HttpClient`.

Example:

```text
package com.vali.shared.features.vacancysearch.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class VacancySearchRemoteDataSource(
    private val httpClient: HttpClient
) {

    suspend fun load() {
        httpClient.get("https://example.com/api/vacancySearch").body<Unit>()
    }
}
```

It also generates shared network core files:

```text
core/network/
 ├── HttpClientFactory.kt
 └── NetworkModule.kt
```

Example `HttpClientFactory`:

```text
package com.vali.shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }
        }
    }
}
```

Example `NetworkModule`:

```text
package com.vali.shared.core.network

import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClientFactory.create()
    }
}
```

## Dependencies for sample project

If Koin and Ktor generation are used, add these dependencies to `commonMain`:

```text
kotlin {
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
```

## Tests

Run plugin tests:

```bash
./gradlew :plugin:test
```

Run sample compilation:

```bash
./gradlew :sample:compileKotlinJvm
```

Check Gradle configuration cache support:

```bash
./gradlew --configuration-cache :sample:createKmpFeature --name CacheTest --architecture mvi --di koin --networking ktor
```

The test suite covers:

- MVI feature generation
- MVVM feature generation
- Ktor + Koin generation
- Architecture conflict protection
- Recreate behavior
- Gradle configuration cache compatibility

## Current status

Version:

```text
1.0.1
```

Version `1.0.1` includes Gradle configuration cache support and JitPack publishing configuration.

Current supported options:

```text
--name
--architecture mvi|mvvm
--di none|koin
--networking none|ktor
--packageName
--outputDir
--recreate
```

## Roadmap

- Generate Compose screen templates
- Generate reducer unit tests
- Generate UseCase tests
- Add local/remote repository strategy
- Add iOS/SwiftUI wrapper generation
- Add Gradle Plugin Portal publishing
- Add optional feature module generation

## License

This project is source-available, but it is not open-source.

You may use the KMP Architecture Generator plugin in personal, educational,
open-source, and commercial projects.

You may use, modify, distribute, and sell the code generated by this plugin as
part of your own projects.

You may not sell, sublicense, rebrand, redistribute, publish modified versions,
or create derivative versions of the plugin itself without explicit written
permission.

Copyright (c) 2026 Vali. All rights reserved.