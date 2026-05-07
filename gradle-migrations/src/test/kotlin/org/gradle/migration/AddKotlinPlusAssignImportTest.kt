package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe in this codebase adds `import org.gradle.kotlin.dsl.plusAssign` for `+=` on
// ListProperty receivers.
class AddKotlinPlusAssignImportTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun addsImportWhenPlusAssignOnListProperty() {
        rewriteRun(
            Assertions.kotlin(
                """
            import org.gradle.api.provider.ListProperty
            class Holder { val p: ListProperty<String> = TODO() }
            fun cfg(h: Holder) {
                h.p += "foo"
            }
                """.trimIndent(),
                """
            import org.gradle.api.provider.ListProperty
            import org.gradle.kotlin.dsl.plusAssign
            class Holder { val p: ListProperty<String> = TODO() }
            fun cfg(h: Holder) {
                h.p += "foo"
            }
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }

    @Test
    fun doesNotAddImportOnUnrelatedPlusAssign() {
        rewriteRun(
            Assertions.kotlin(
                """
            fun cfg() {
                var n: Int = 0
                n += 1
            }
                """.trimIndent()
            )
        )
    }
}
