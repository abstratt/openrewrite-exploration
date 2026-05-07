package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe rewrites Kotlin DSL `maxHeapSize = maxHeapSize.uppercase()` self-reference
// to `maxHeapSize.set(maxHeapSize.get().uppercase())`.
class MigrateScalarPropertySelfReferenceTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
        spec.typeValidationOptions(
            TypeValidation.builder()
                .methodInvocations(false)
                .identifiers(false)
                .variableDeclarations(false)
                .build(),
        )
    }

    @Test
    fun rewritesScalarSelfReferenceInsideTypedScope() {
        rewriteRun(
            Assertions.kotlin(
                """
            import org.gradle.api.tasks.testing.Test
            tasks.withType<Test> {
                maxHeapSize = maxHeapSize.uppercase()
            }
                """.trimIndent(),
                """
            import org.gradle.api.tasks.testing.Test
            tasks.withType<Test> {
                maxHeapSize.set(maxHeapSize.get().uppercase())
            }
                """.trimIndent()
            ) {
                it.path("build.gradle.kts")
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }

    @Test
    fun doesNotRewriteNonSelfReference() {
        rewriteRun(
            Assertions.kotlin(
                """
            import org.gradle.api.tasks.testing.Test
            tasks.withType<Test> {
                maxHeapSize = "1g".uppercase()
            }
                """.trimIndent()
            ) {
                it.path("build.gradle.kts")
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }
}
