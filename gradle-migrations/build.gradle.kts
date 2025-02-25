plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:latest.release"))
    implementation(kotlin("stdlib"))
    implementation("org.openrewrite:rewrite-core:8.45.1")
    implementation("org.openrewrite:rewrite-kotlin:1.27.0")
    implementation("org.openrewrite:rewrite-java:8.45.1")
    runtimeOnly("org.openrewrite:rewrite-java-17:8.47.1")
    implementation("org.openrewrite.meta:rewrite-analysis:2.17.0")
    implementation("org.openrewrite.meta:rewrite-analysis:2.17.0")
    //implementation("org.openrewrite.rewrite:org.openrewrite.rewrite.gradle.plugin:7.1.4")
    testImplementation("org.openrewrite:rewrite-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    val plugin by plugins.creating {
        id = "org.gradle.migration"  // Plugin ID
        implementationClass = "org.gradle.migration.plugin.MigrationPlugin"  // The class that implements the plugin
    }
}
