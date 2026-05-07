package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe in this codebase attaches a TODO marker to subclass overrides of removed setters.
class DetectSetterOverrideTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun flagsOverrideOfRemovedSetter() {
        rewriteRun(
            Assertions.java(
                """
            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;
            public class MyTest extends Test {
                @Override
                public void setClasspath(FileCollection cp) {
                }
            }
                """.trimIndent(),
                """
            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;
            public class MyTest extends Test {
                /* TODO: Override of `setClasspath` on subclass of Test. */
                @Override
                public void setClasspath(FileCollection cp) {
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotFlagUnrelatedSetters() {
        rewriteRun(
            Assertions.java(
                """
            import org.gradle.api.tasks.testing.Test;
            public class MyTest extends Test {
                private String name;
                public void setName(String n) { this.name = n; }
            }
                """.trimIndent()
            )
        )
    }
}
