package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe in this codebase rewrites `args += v` to `args.add(v)` for ListProperty.
class MigrateListPropertyPlusAssignTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
        spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build())
    }

    @Test
    fun rewritesPlusAssignOnListProperty() {
        rewriteRun(
            Assertions.java(
                """
            import org.gradle.api.provider.ListProperty;
            class Build {
                void cfg(ListProperty<String> args, String v) {
                    args += v;
                }
            }
                """.trimIndent(),
                """
            import org.gradle.api.provider.ListProperty;
            class Build {
                void cfg(ListProperty<String> args, String v) {
                    args.add(v);
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotRewritePlusAssignOnRegularInt() {
        rewriteRun(
            Assertions.java(
                """
            class Build {
                void cfg() {
                    int n = 0;
                    n += 1;
                }
            }
                """.trimIndent()
            )
        )
    }
}
