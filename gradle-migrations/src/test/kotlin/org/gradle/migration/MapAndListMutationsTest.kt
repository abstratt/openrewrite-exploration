package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// Borrowed from asodja/openrewrite-provider-api-migration. Currently expected to fail —
// no recipe in this codebase rewrites the simple list/map mutations: clear()->empty(),
// asList()->get(), indexed-assign->put().
class MapAndListMutationsTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
    }

    @Test
    fun mapPropertyClearBecomesEmpty() {
        rewriteRun(
            Assertions.java(
                """
            import org.gradle.api.provider.MapProperty;
            class Build {
                void cfg(MapProperty<String, Object> sys) {
                    sys.clear();
                }
            }
                """.trimIndent(),
                """
            import org.gradle.api.provider.MapProperty;
            class Build {
                void cfg(MapProperty<String, Object> sys) {
                    sys.empty();
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun listPropertyClearBecomesEmpty() {
        rewriteRun(
            Assertions.java(
                """
            import org.gradle.api.provider.ListProperty;
            class Build {
                void cfg(ListProperty<String> args) {
                    args.clear();
                }
            }
                """.trimIndent(),
                """
            import org.gradle.api.provider.ListProperty;
            class Build {
                void cfg(ListProperty<String> args) {
                    args.empty();
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun asListBecomesGet() {
        rewriteRun(
            Assertions.java(
                """
            import java.util.List;
            import org.gradle.api.provider.ListProperty;
            class Build {
                List<String> cfg(ListProperty<String> args) {
                    return args.asList();
                }
            }
                """.trimIndent(),
                """
            import java.util.List;
            import org.gradle.api.provider.ListProperty;
            class Build {
                List<String> cfg(ListProperty<String> args) {
                    return args.get();
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun clearDoesNotFireOnRegularList() {
        rewriteRun(
            Assertions.java(
                """
            import java.util.ArrayList;
            import java.util.List;
            class Build {
                void cfg() {
                    List<String> l = new ArrayList<>();
                    l.clear();
                }
            }
                """.trimIndent()
            )
        )
    }
}
