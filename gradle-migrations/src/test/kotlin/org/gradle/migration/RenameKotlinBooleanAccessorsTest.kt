package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe in this codebase renames Kotlin boolean accessor calls (isX -> x) on types whose
// `boolean isX()` method was removed in favor of a `Property<Boolean> getX()`.
class RenameKotlinBooleanAccessorsTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun renamesIsEnabledOnTestTask() {
        rewriteRun(
            Assertions.kotlin(
                """
            import org.gradle.api.tasks.testing.Test
            fun cfg(t: Test) {
                t.isFailOnNoMatchingTests
            }
                """.trimIndent(),
                """
            import org.gradle.api.tasks.testing.Test
            fun cfg(t: Test) {
                t.failOnNoMatchingTests
            }
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }

    @Test
    fun doesNotRenameOnUnrelatedTypes() {
        rewriteRun(
            Assertions.kotlin(
                """
            class Foo {
                val isEnabled: Boolean get() = true
            }
            fun cfg(f: Foo) {
                f.isEnabled
            }
                """.trimIndent()
            )
        )
    }
}
