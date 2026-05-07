repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.assignment") version "2.3.21" apply false
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            jvmToolchain(21)
        }
    }
}
