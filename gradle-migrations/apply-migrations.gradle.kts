val pluginJar = file("build/libs/gradle-migrations.jar")
initscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.openrewrite:plugin:latest.release")
    }
}


rootProject {
    apply<org.openrewrite.gradle.RewritePlugin>()
    require(pluginJar.exists()) {
        println("$pluginJar not found")
    }
    afterEvaluate {
        if (repositories.isEmpty()) {
            repositories {
                mavenCentral()
            }
        }
    }
    dependencies {
        "rewrite"(files(pluginJar))
    }
    project.extensions.findByType(org.openrewrite.gradle.RewriteExtension::class.java)?.let { rewrite ->
        // Configure the extension
        rewrite.activeRecipe(
            "org.openrewrite.FindParseFailures",
            "org.gradle.migration.Gradle8to9"
        )
        rewrite.setExportDatatables(true)
    }
}




