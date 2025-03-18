package org.gradle.migration

import com.fasterxml.jackson.annotation.JsonProperty
import org.gradle.internal.extensions.stdlib.capitalized
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType
import org.openrewrite.java.tree.JavaType.Method
import org.openrewrite.kotlin.KotlinIsoVisitor

@Suppress("unused")
class KotlinAddOperatorImports constructor(@JsonProperty("targetType") val targetType: String, @JsonProperty("propertyName") val propertyName: String) : Recipe() {
    override fun getDisplayName(): String {
        return "Adds static import for operator-backing extension functions"
    }

    override fun getDescription(): String {
        return "$displayName."
    }

    override fun getVisitor(): KotlinIsoVisitor<ExecutionContext> {
        val candidateSetter = "set${propertyName.capitalized()}"
        return object : KotlinIsoVisitor<ExecutionContext>() {
            override fun visitAssignment(assignment: J.Assignment, p: ExecutionContext): J.Assignment {
                var addImport = false
                assignment.variable.accept(object : KotlinIsoVisitor<ExecutionContext>() {
                    override fun visitFieldAccess(fieldAccess: J.FieldAccess, p: ExecutionContext): J.FieldAccess {
                        val target = fieldAccess.target
                        val targetType = target.type as JavaType.Class?
                        if (targetType !== null) {
                            val allMembers = targetType.allMethods()
                            val resolvedSetter = allMembers.find {
                                it.name == candidateSetter
                            }
                            if (resolvedSetter is Method) {
                                addImport = true
                            }
                        }
                        return fieldAccess
                    }
                }, p)
                if (addImport) {
                    maybeAddImport("org.gradle.kotlin.dsl.assign", false)
                }
                return super.visitAssignment(assignment, p)
            }
        }
    }
}

fun JavaType.FullyQualified.allMethods(): Sequence<Method> {
    val inherited = interfaces
        .asSequence()
        .flatMap { it.allMethods() }
    return inherited + (supertype?.allMethods() ?: emptySequence()) + methods.asSequence()
}