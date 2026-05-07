package org.gradle.migration

import com.fasterxml.jackson.annotation.JsonProperty
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType
import org.openrewrite.java.tree.JavaType.Method
import org.openrewrite.kotlin.KotlinIsoVisitor

/**
 * Adds `import org.gradle.kotlin.dsl.assign` to a Kotlin file when the file assigns to a
 * field whose declared receiver type now exposes a `Property<T>` getter — without the
 * import, the operator-extension that delegates `t.x = v` to `t.getX().set(v)` isn't in
 * scope.
 *
 * **Languages:** Kotlin only. The visitor extends `KotlinIsoVisitor`, which is only
 * dispatched by the Kotlin parser. Java and Groovy sources are not visited.
 */
@Suppress("unused")
class KotlinAddOperatorImports constructor(@JsonProperty("targetType") val targetType: String, @JsonProperty("propertyName") val propertyName: String) : Recipe() {
    override fun getDisplayName(): String {
        return "Adds static import for operator-backing extension functions"
    }

    override fun getDescription(): String {
        return "$displayName."
    }

    override fun getVisitor(): KotlinIsoVisitor<ExecutionContext> {
        val candidateSetter = "set${propertyName.replaceFirstChar(Char::uppercaseChar)}"
        return object : KotlinIsoVisitor<ExecutionContext>() {
            override fun visitAssignment(assignment: J.Assignment, p: ExecutionContext): J.Assignment {
                var containsAssignment = false
                assignment.variable.accept(object : KotlinIsoVisitor<ExecutionContext>() {
                    override fun visitFieldAccess(fieldAccess: J.FieldAccess, p: ExecutionContext): J.FieldAccess {
                        val target = fieldAccess.target
                        val targetType = findClass(target.type)
                        if (targetType !== null) {
                            val allMembers = targetType.allMethods()
                            val resolvedSetter = allMembers.find {
                                it.name == candidateSetter
                            }
                            if (resolvedSetter is Method) {
                                containsAssignment = true
                            }
                        }
                        return fieldAccess
                    }
                }, p)
                if (containsAssignment) {
                    maybeAddImport("org.gradle.kotlin.dsl.assign", false)
                }
                return super.visitAssignment(assignment, p)
            }
        }
    }

    private fun findClass(javaType: JavaType?): JavaType.Class? = when(javaType) {
        is JavaType.Class -> javaType
        is JavaType.Parameterized -> findClass(javaType.type)
        else -> null
    }
}

fun JavaType.FullyQualified.allMethods(): Sequence<Method> {
    val inherited = interfaces
        .asSequence()
        .flatMap { it.allMethods() }
    return inherited + (supertype?.allMethods() ?: emptySequence()) + methods.asSequence()
}