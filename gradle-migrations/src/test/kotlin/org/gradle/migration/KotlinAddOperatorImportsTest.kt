package org.gradle.migration

import org.junit.jupiter.api.Test
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
            "org.gradle.migration.Gradle8to9")
        spec.afterTypeValidationOptions(TypeValidation.all().methodInvocations(false))
    }

    @Test
    fun addImport() {
        rewriteRun(
            Assertions.kotlin(
                """
            package com.yourorg
            
            import org.gradle.api.tasks.compile.JavaCompile
            
            class MyClass {
                fun use(javaCompile: JavaCompile) {
                    javaCompile.options.isIncremental = true
                }
            }
                
                """.trimIndent(),
                """
            package com.yourorg
            
            import org.gradle.api.tasks.compile.JavaCompile
            import org.gradle.kotlin.dsl.assign

            class MyClass {
                fun use(javaCompile: JavaCompile) {
                    javaCompile.options.isIncremental = true
                }
            }
                
                """.trimIndent()
            ) {
                (it.parser as KotlinParser.Builder).classpath(JavaParser.runtimeClasspath())
            }
        )
    }
}