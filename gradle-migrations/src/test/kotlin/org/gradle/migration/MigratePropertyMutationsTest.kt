package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe in this codebase rewrites Map/List property mutations (remove, compute, ...) that
// don't exist on the lazy MapProperty/ListProperty API.
class MigratePropertyMutationsTest : RewriteTest {
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
    fun rewritesMapPropertyRemove() {
        rewriteRun(
            Assertions.kotlin(
                """
            import org.gradle.api.provider.MapProperty
            fun cfg(environment: MapProperty<String, Any>) {
                environment.remove("RUNNER_TEMP")
            }
                """.trimIndent(),
                """
            import org.gradle.api.provider.MapProperty
            fun cfg(environment: MapProperty<String, Any>) {
                val updated = environment.get().toMutableMap()
                updated.remove("RUNNER_TEMP")
                environment.set(updated)
            }
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }
}
