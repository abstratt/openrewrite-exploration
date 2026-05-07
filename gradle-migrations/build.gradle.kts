plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    //implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:latest.release"))
    implementation(kotlin("stdlib"))
    implementation("org.openrewrite:rewrite-core:8.81.7")
    implementation("org.openrewrite:rewrite-kotlin:8.81.7")
    implementation("org.openrewrite:rewrite-groovy:8.81.7")
    implementation("org.openrewrite:rewrite-java:8.81.7")
    implementation("org.openrewrite.meta:rewrite-analysis:2.23.0")
    testImplementation(gradleApi())
    testImplementation("org.openrewrite:rewrite-java-21:8.81.7")
    testImplementation("org.openrewrite:rewrite-test:8.81.7")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.1")
}

tasks.test {
    useJUnitPlatform()
}
