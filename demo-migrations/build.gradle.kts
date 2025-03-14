plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.openrewrite:rewrite-core:8.47.2")
    implementation("org.openrewrite:rewrite-kotlin:1.28.0")
    implementation("org.openrewrite:rewrite-java:8.47.2")
    implementation("org.openrewrite.meta:rewrite-analysis:2.17.0")
}
