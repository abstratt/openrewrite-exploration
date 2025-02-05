package org.gradle.migration

import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType
import org.openrewrite.kotlin.KotlinIsoVisitor

class ImportAssign : Recipe() {
    override fun getDisplayName(): String = "Add assign import to Kotlin files"
    override fun getDescription(): String = "Adds `import org.example.utils.assign` to Kotlin files that reference `setMyProperty`."

    override fun getVisitor(): KotlinIsoVisitor<ExecutionContext> {
        println("getVisitor() ${this}")
        return object : KotlinIsoVisitor<ExecutionContext>() {
            override fun visitAssignment(assignment: J.Assignment, p: ExecutionContext): J.Assignment {
                val r = super.visitAssignment(assignment, p)
                if (assignment.variable is J.FieldAccess) {
                    val asFieldAccess: J.FieldAccess = assignment.variable as J.FieldAccess
                    val targetType = asFieldAccess.target.type as JavaType.Class
                    if (targetType.className.contentEquals("MyTask") && targetType.packageName.contentEquals("org.example")) {
                        maybeAddImport("org.example.utils.assign", false)
                    }
                }
                return r
            }
        }
    }
}