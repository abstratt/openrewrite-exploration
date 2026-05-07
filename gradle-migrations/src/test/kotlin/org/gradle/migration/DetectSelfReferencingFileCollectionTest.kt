package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe in this codebase attaches a TODO marker to self-referencing
// ConfigurableFileCollection.setFrom(...) calls.
class DetectSelfReferencingFileCollectionTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun flagsSelfReferencingSetFrom() {
        rewriteRun(
            Assertions.java(
                """
            import org.gradle.api.file.ConfigurableFileCollection;
            class Build {
                void cfg(ConfigurableFileCollection classpath, Object extra) {
                    classpath.setFrom(extra, classpath);
                }
            }
                """.trimIndent(),
                """
            import org.gradle.api.file.ConfigurableFileCollection;
            class Build {
                void cfg(ConfigurableFileCollection classpath, Object extra) {
                    /* TODO: Self-referencing ConfigurableFileCollection. */
                    classpath.setFrom(extra, classpath);
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotFlagNonSelfReferencingSetFrom() {
        rewriteRun(
            Assertions.java(
                """
            import org.gradle.api.file.ConfigurableFileCollection;
            class Build {
                void cfg(ConfigurableFileCollection classpath, Object extra) {
                    classpath.setFrom(extra);
                }
            }
                """.trimIndent()
            )
        )
    }
}
