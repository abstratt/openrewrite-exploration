plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.openrewrite:rewrite-core:8.45.1")
    implementation("org.openrewrite:rewrite-kotlin:1.27.0")
    implementation("org.openrewrite:rewrite-java:8.45.1")
    implementation("org.openrewrite.meta:rewrite-analysis:2.17.0")
}
