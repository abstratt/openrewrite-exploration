package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

// Borrowed from asodja/openrewrite-provider-api-migration. Smoke test for the aggregate
// recipe — makes sure Gradle9to10 loads cleanly and rewrites a representative
// multi-pattern input end-to-end.
class MigrateToProviderApiMetaTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
        spec.afterTypeValidationOptions(TypeValidation.all().methodInvocations(false))
    }

    @Test
    fun rewritesMultipleSetterKindsViaAggregate() {
        rewriteRun(
            Assertions.java(
                """
            import java.util.List;
            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, FileCollection cp, List<String> jvmArgs) {
                    t.setMaxParallelForks(4);
                    t.setClasspath(cp);
                    t.setJvmArgs(jvmArgs);
                }
            }
                """.trimIndent(),
                """
            import java.util.List;
            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, FileCollection cp, List<String> jvmArgs) {
                    t.getMaxParallelForks().set(4);
                    t.getClasspath().setFrom(cp);
                    t.getJvmArgs().set(jvmArgs);
                }
            }
                """.trimIndent()
            )
        )
    }
}
