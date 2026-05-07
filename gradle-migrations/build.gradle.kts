plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    //implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:latest.release"))
    implementation(kotlin("stdlib"))
    implementation("org.openrewrite:rewrite-core:8.56.1")
    implementation("org.openrewrite:rewrite-kotlin:8.56.1")
    implementation("org.openrewrite:rewrite-groovy:8.56.1")
    implementation("org.openrewrite:rewrite-java:8.56.1")
    implementation("org.openrewrite.meta:rewrite-analysis:2.23.0")
    testImplementation("org.openrewrite:rewrite-java-21:8.56.1")
    testImplementation("org.openrewrite:rewrite-test:8.56.1")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.1")
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
