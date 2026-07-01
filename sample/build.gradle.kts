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