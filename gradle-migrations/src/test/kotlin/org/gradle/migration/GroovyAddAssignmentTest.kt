package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.groovy.Assertions

class GroovyAddAssignmentTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle8to9")
    }

    @Test
    fun introducesAssignmentForPropertyCallWithNoParentheses() {
        rewriteRun(
            Assertions.groovy(
                """
            package com.yourorg
            
            class MyClass {
                void setProp1(Integer value) {
                }
            }
            
            def val1 = new MyClass()
            
            val1.prop1 10
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
            
                """.trimIndent(),
                """
            package com.yourorg
            
            class MyClass {
                void setProp1(Integer value) {
                }
            }
            
            def val1 = new MyClass()
            
            val1.prop1 = 10
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotIntroduceAssignmentForMultiEntryMapValueWithoutBrackets() {
        rewriteRun(
            Assertions.groovy(
                """
            package com.yourorg
            
            class OtherClass {
                String propA
                Integer propB
            }
            
            class MyClass {
                void setProp1(OtherClass value) {
                }
            }
            
            def val1 = new MyClass()
            
            val1.prop1(propA: "foo", propB: 10)
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
            
                """.trimIndent()
            )
        )
    }

    @Test
    fun introducesAssignmentForMultiEntryMapValueWithBrackets() {
        rewriteRun(
            Assertions.groovy(
                """
            package com.yourorg
            
            class OtherClass {
                String propA
                Integer propB
            }
            
            class MyClass {
                void setProp1(OtherClass value) {
                }
            }
            
            def val1 = new MyClass()
            
            val1.prop1([propA: "foo", propB: 20])
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
            
                """.trimIndent(),
                """
            package com.yourorg
            
            class OtherClass {
                String propA
                Integer propB
            }
            
            class MyClass {
                void setProp1(OtherClass value) {
                }
            }
            
            def val1 = new MyClass()
            
            val1.prop1 = [propA: "foo", propB: 20]
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotIntroduceAssignmentIfAMethodExists() {
        rewriteRun(
            Assertions.groovy(
                """
            package com.yourorg
            
            class MyClass {
                void setProp1(Integer newProp1Value) {
                }
                void prop1(Integer newProp1Value) {
                }
            }
            
            def val1 = new MyClass()
            
            // should not change
            val1.prop1(10)
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
            
                """.trimIndent()
            )
        )
    }

    @Test
    fun introducesAssignmentForParenthesizedArguments() {
        rewriteRun(
            Assertions.groovy(
                """
            package com.yourorg
            
            class MyClass {
                void setProp1(Integer newProp1Value) {
                }
            }
            
            def val1 = new MyClass()
            
            val1.prop1(10)
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
            
                """.trimIndent(),
                """
            package com.yourorg
            
            class MyClass {
                void setProp1(Integer newProp1Value) {
                }
            }
            
            def val1 = new MyClass()
            
            val1.prop1 = 10
            
            def someFunction(String par1) {}
            
            // should not change
            someFunction "Hello"
            
                """.trimIndent()
            )
        )
    }
}