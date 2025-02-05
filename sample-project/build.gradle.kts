plugins {
    kotlin("jvm")
    kotlin("plugin.assignment")
    id("org.openrewrite.rewrite") version "7.0.5"
}

assignment {
    annotation("org.example.AssignOpAnnotation")
}

rewrite {
    activeRecipe(
        "org.gradle.migration.ImportAssign",
        //"org.gradle.kotlin.AddAssignImportRecipe",
        "org.openrewrite.FindParseFailures"
        //"org.openrewrite.java.search.FindMissingTypes"
    )
    setExportDatatables(true)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    rewrite(project(":gradle-migrations"))
//    implementation("org.openrewrite:rewrite-core:8.44.2")
//    implementation("org.openrewrite:rewrite-kotlin:1.26.0")
//    implementation("org.openrewrite:rewrite-java:8.44.2")
//    implementation("org.openrewrite.meta:rewrite-analysis:2.16.0")
}


// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
