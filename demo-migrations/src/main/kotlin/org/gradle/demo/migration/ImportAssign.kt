package org.gradle.demo.migration

import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType
import org.openrewrite.java.tree.JavaType.Variable
import org.openrewrite.kotlin.KotlinIsoVisitor

@Suppress("unused")
class ImportAssign : Recipe() {
    override fun getDisplayName(): String = "Add assign import to Kotlin files"
    override fun getDescription(): String = "Adds `import org.example.utils.assign` to Kotlin files that reference `setMyProperty`."

    override fun getVisitor(): KotlinIsoVisitor<ExecutionContext> {
        return object : KotlinIsoVisitor<ExecutionContext>() {
            override fun visitAssignment(assignment: J.Assignment, p: ExecutionContext): J.Assignment {
                println("Running visitor against $assignment")
                val r = super.visitAssignment(assignment, p)
                addImportBasedOnPropertyAnnotation(assignment)
                return r
            }
        }
    }

    private fun KotlinIsoVisitor<ExecutionContext>.addImportBasedOnTargetProperty(assignment: J.Assignment) {
        if (assignment.variable is J.FieldAccess) {
            val asFieldAccess: J.FieldAccess = assignment.variable as J.FieldAccess
            val targetType = asFieldAccess.target.type as JavaType.Class
            if (targetType.className.contentEquals("MyTask") && targetType.packageName.contentEquals("org.example")) {
                maybeAddImport("org.example.utils.assign", false)
            }
        }
    }

    private fun KotlinIsoVisitor<ExecutionContext>.addImportBasedOnPropertyAnnotation(assignment: J.Assignment) {
        if (assignment.variable is J.FieldAccess) {
            val asFieldAccess: J.FieldAccess = assignment.variable as J.FieldAccess
            if (asFieldAccess.target is J.Identifier) {
                val targetType = asFieldAccess.target.type as JavaType.Class
                val resolvedProperty = targetType.members.find {
                    it.name == "myProperty"
                }
                if (resolvedProperty is Variable) {
                    val needsImport =
                        resolvedProperty.annotations.any { it.fullyQualifiedName == "org.example.ToBeMigrated" }
                    if (needsImport) {
                        maybeAddImport("org.example.utils.assign", false)
                    }
                }
            }
        }
    }
}