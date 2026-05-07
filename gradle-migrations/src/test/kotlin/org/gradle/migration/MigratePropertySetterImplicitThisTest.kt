package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

// Borrowed from asodja/openrewrite-provider-api-migration. Negative case: implicit-this setter
// calls whose receiver type can't be resolved must NOT be rewritten — name-only catalog matching
// would produce false positives.
class MigratePropertySetterImplicitThisTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
        spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build())
    }

    @Test
    fun doesNotRewriteImplicitSetterWhenReceiverTypeUnknown() {
        rewriteRun(
            Assertions.kotlin(
                """
            fun cfg() {
                val anyBlock: () -> Unit = {
                    setMaxHeapSize("1024m")
                }
                anyBlock()
            }
                """.trimIndent()
            ) {
                it.path("build.gradle.kts")
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }
}
