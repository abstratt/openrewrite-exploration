package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Tests that explicit-setter calls
// in Kotlin source (t.setMaxParallelForks(4)) get migrated the same way as Java.
class MigratePropertySetterKotlinTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun migratesExplicitSetterCallInKotlin() {
        rewriteRun(
            Assertions.kotlin(
                """
            import org.gradle.api.tasks.testing.Test
            fun cfg(t: Test) {
                t.setMaxParallelForks(4)
            }
                """.trimIndent(),
                """
            import org.gradle.api.tasks.testing.Test
            fun cfg(t: Test) {
                t.getMaxParallelForks().set(4)
            }
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }
}
