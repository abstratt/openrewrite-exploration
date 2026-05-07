package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe rewrites Kotlin `t.delete = x` to `t.targetFiles.setFrom(x)` for Delete tasks.
class MigrateDeleteTaskToTargetFilesTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun rewritesDeleteAssignmentToTargetFilesSetFrom() {
        rewriteRun(
            Assertions.kotlin(
                """
            import org.gradle.api.tasks.Delete
            fun cfg(t: Delete, x: Any) {
                t.delete = x
            }
                """.trimIndent(),
                """
            import org.gradle.api.tasks.Delete
            fun cfg(t: Delete, x: Any) {
                t.targetFiles.setFrom(x)
            }
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }

    @Test
    fun doesNotRewriteDeleteAssignmentOnOtherTypes() {
        rewriteRun(
            Assertions.kotlin(
                """
            class Foo { var delete: Any = Unit }
            fun cfg(f: Foo, x: Any) {
                f.delete = x
            }
                """.trimIndent()
            )
        )
    }
}
