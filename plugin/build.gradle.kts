plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.vali"
version = "1.0.1"


dependencies {
    implementation("junit:junit:4.12")
    implementation("org.junit.jupiter:junit-jupiter:5.14.0")
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("kmpArchitectureGenerator") {
            id = "com.vali.kmp-architecture-generator"
            implementationClass = "com.vali.kmpgenerator.KmpArchitectureGeneratorPlugin"
        }
    }
}
tasks.test {
    useJUnitPlatform()
}