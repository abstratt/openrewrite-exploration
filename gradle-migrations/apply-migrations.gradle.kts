val pluginJar = file("build/libs/gradle-migrations.jar")
initscript {
//    val pluginJar = file("build/libs/gradle-migrations.jar")
    repositories {
//        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
//        classpath(files(pluginJar))
//        classpath("org.openrewrite:rewrite-core:8.45.1")
//        classpath("org.openrewrite:rewrite-kotlin:1.27.0")
//        classpath("org.openrewrite:rewrite-java:8.45.1")
//        classpath("org.openrewrite.meta:rewrite-analysis:2.17.0")
//        classpath("org.openrewrite.meta:rewrite-analysis:2.17.0")
        classpath("org.openrewrite.rewrite:org.openrewrite.rewrite.gradle.plugin:7.1.4")
    }
//    if (!pluginJar.exists()) {
//        println("Plugin JAR not found at $pluginJar")
//    }
}

//beforeSettings {
//    apply<org.gradle.migration.plugin.MigrationPlugin>()
//}

rootProject {
    apply<org.openrewrite.gradle.RewritePlugin>()
    require(pluginJar.exists()) {
        println("$pluginJar not found")
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        "rewrite"(files(pluginJar))
    }
    project.extensions.findByType(org.openrewrite.gradle.RewriteExtension::class.java)?.let { rewrite ->
        // Configure the extension
        rewrite.activeRecipe(
            "org.gradle.migration.Gradle8to9"
        )
        rewrite.setExportDatatables(true)
    }
}

settingsEvaluated {
    println("Plugins")
    (pluginManager as org.gradle.api.internal.plugins.PluginManagerInternal).pluginContainer.forEach {
        println(it)
    }
}

