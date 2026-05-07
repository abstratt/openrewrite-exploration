package org.gradle.migration

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.openrewrite.java.JavaParser
import org.openrewrite.kotlin.Assertions
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

/**
 * Kotlin code in Gradle 8 that worked like this:
 *
 * someTask.someProperty = someValue
 *
 * If `someProperty` is migrated to a lazy property, the same code would still work, as long
 * as the following static import is added:
 *
 * import org.gradle.kotlin.dsl.assign
 */
class KotlinAddOperatorImportsTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(KotlinAddOperatorImportsTest::class.java.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10")
    }

    @ParameterizedTest
    @CsvSource(
        "org.gradle.api.tasks.compile.JavaCompile, options.isIncremental, true",
        "org.gradle.api.tasks.testing.Test, maxHeapSize, true",
    )
    fun addImport(targetType: String, path: String, value: String) {
        rewriteRun(
            Assertions.kotlin(
                """
            package com.yourorg
            
            class MyClass {
                fun use(target: $targetType) {
                    target.$path = $value
                }
            }
                
                """.trimIndent(),
                """
            package com.yourorg
            
            import org.gradle.kotlin.dsl.assign

            class MyClass {
                fun use(target: $targetType) {
                    target.$path = $value
                }
            }
                
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }

    @Test
    fun doesNotAddImportWhenAlreadyPresent() {
        rewriteRun(
            Assertions.kotlin(
                """
            package com.yourorg

            import org.gradle.api.tasks.testing.Test
            import org.gradle.kotlin.dsl.assign

            class MyClass {
                fun use(t: Test) {
                    t.maxHeapSize = "1g"
                }
            }
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }

    @Test
    fun doesNotAddImportWhenNoPropertyAssignment() {
        rewriteRun(
            Assertions.kotlin(
                """
            package com.yourorg

            fun cfg() {
                var s: String = "foo"
                s = "bar"
            }
                """.trimIndent()
            )
        )
    }
}