package org.gradle.migration.plugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.migration.JavaConvertToLazyProperty
import org.gradle.nativeplatform.Repositories

class MigrationPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
//        settings.pluginManagement.plugins {
//            it.id("org.openrewrite.rewrite").version("7.0.5")
//        }
//        settings.pluginManagement.repositories {
//            it.mavenCentral()
//            it.gradlePluginPortal()
//        }
        settings.pluginManagement.apply {
            plugins.apply {
                id("org.openrewrite.rewrite").apply(false)
            }
            repositories.apply {
                gradlePluginPortal()
            }
        }
        settings.gradle.beforeProject { project ->
            project.repositories.apply {
                mavenCentral()
            }
            if (project.parent == null) {
                //project.plugins.apply(org.openrewrite.gradle.RewritePlugin::class.java)
                //project.plugins.apply("org.openrewrite.rewrite")
//                val self = project.files(this.javaClass.protectionDomain.codeSource.location)
//                println("Adding ${self.files} as a dependency")
//                project.dependencies.add("rewrite", self)
//                project.extensions.findByType(org.openrewrite.gradle.RewriteExtension::class.java)?.let { rewrite ->
//                    // Configure the extension
//                    rewrite.activeRecipe(
//                        "org.gradle.migration.Gradle8to9"
//                    )
//                    rewrite.setExportDatatables(true)
//                }
            }
        }
    }
}