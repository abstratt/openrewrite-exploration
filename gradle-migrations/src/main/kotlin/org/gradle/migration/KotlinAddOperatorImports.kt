package org.gradle.migration

import com.fasterxml.jackson.annotation.JsonProperty
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType
import org.openrewrite.java.tree.JavaType.Variable
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
        return object : KotlinIsoVisitor<ExecutionContext>() {
            override fun visitAssignment(assignment: J.Assignment, p: ExecutionContext): J.Assignment {
                var addImport = false
                assignment.variable.accept(object : KotlinIsoVisitor<ExecutionContext>() {
                    override fun visitFieldAccess(fieldAccess: J.FieldAccess, p: ExecutionContext): J.FieldAccess {
                        val target = fieldAccess.target
                        val targetType = target.type as JavaType.Class?
                        if (targetType !== null) {
                            val resolvedProperty = targetType.members.find {
                                it.name == propertyName
                            }
                            if (resolvedProperty is Variable) {
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