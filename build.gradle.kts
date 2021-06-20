import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
    application
}

group = "nl.marc_apps"
version = "0.1"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

application {
    mainClass.set("MainKt")
}

detekt {
    toolVersion = "1.17.1"

    config.setFrom(files("$rootDir/config/detekt/detekt.yaml"))

    basePath = projectDir.path

    buildUponDefaultConfig = true

    reports {
        sarif {
            enabled = true
            destination = file("build/reports/detekt/detekt.sarif.json")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

    implementation("io.insert-koin:koin-core:3.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.2.1")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.82.0")
}
