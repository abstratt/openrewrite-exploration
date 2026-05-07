package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.groovy.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe rewrites Groovy `t.source = x` (a property-assignment that became a method-call
// post-migration) to `t.setSource(x)`.
class MigrateSourceAssignmentTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun rewritesSourceAssignmentInGroovy() {
        rewriteRun(
            Assertions.groovy(
                """
            import org.gradle.api.tasks.SourceTask
            def cfg(SourceTask t, Object x) {
                t.source = x
            }
                """.trimIndent(),
                """
            import org.gradle.api.tasks.SourceTask
            def cfg(SourceTask t, Object x) {
                t.setSource(x)
            }
                """.trimIndent()
            )
        )
    }
}
