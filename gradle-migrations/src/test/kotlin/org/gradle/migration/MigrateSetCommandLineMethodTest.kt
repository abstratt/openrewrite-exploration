package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// our recipe converts setCommandLine to getCommandLine().set(...) (the lazy-property pattern),
// while the expected migration is a method rename to commandLine(...).
class MigrateSetCommandLineMethodTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun renamesSetCommandLineOnExecSpec() {
        rewriteRun(
            Assertions.java(
                """
            import java.util.List;
            import org.gradle.process.ExecSpec;

            class Build {
                void cfg(ExecSpec exec, List<String> cmd) {
                    exec.setCommandLine(cmd);
                }
            }
                """.trimIndent(),
                """
            import java.util.List;
            import org.gradle.process.ExecSpec;

            class Build {
                void cfg(ExecSpec exec, List<String> cmd) {
                    exec.commandLine(cmd);
                }
            }
                """.trimIndent()
            )
        )
    }
}
