package org.gradle.migration

import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.internal.impldep.org.yaml.snakeyaml.error.Mark
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.Tree.randomId
import org.openrewrite.groovy.GroovyVisitor
import org.openrewrite.groovy.tree.G
import org.openrewrite.groovy.tree.G.MapEntry
import org.openrewrite.groovy.tree.G.MapLiteral
import org.openrewrite.java.tree.*
import org.openrewrite.marker.Markers


/**
 * @see https://docs.gradle.org/current/userguide/upgrading_version_8.html#groovy_space_assignment_syntax
 */
@Suppress("unused")
class GroovyAddAssignment : Recipe() {
    override fun getDisplayName(): String {
        return "Replaces 'prop1 value' with 'prop1 = value'"
    }

    override fun getDescription(): String {
        return "$displayName."
    }

    override fun getVisitor(): GroovyVisitor<ExecutionContext> {
        return object : GroovyVisitor<ExecutionContext>() {
            override fun visitMethodInvocation(method: J.MethodInvocation, ctx: ExecutionContext): J {
                if (method.methodType === null) {
                    // there is no backing method
                    val selectType = method.select?.type
                    if (selectType is JavaType.Class) {
                        val setter = selectType.visibleMethods.asSequence()
                            .find { it.name == "set${method.name.simpleName.capitalized()}" }
                        if (setter !== null) {
                            if (method.arguments.size == 1) {
                                val name = method.name.simpleName
                                val singleArgument = method.arguments[0]
                                // skip named arguments
                                if (!(singleArgument is G.MapEntry)) {
                                    return doRewrite(name, method, singleArgument, ctx)
                                }
                            }
                        }
                    }
                }
                return super.visitMethodInvocation(method, ctx)
            }

            private fun doRewrite(
                name: String,
                method: J.MethodInvocation,
                singleArgument: Expression,
                ctx: ExecutionContext
            ): J.Assignment {
                val variable =
                    JavaType.Variable(null, 0, name, method.type, singleArgument.type, emptyList())
                val variableExpression =
                    J.Identifier(
                        randomId(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        emptyList(),
                        name,
                        null,
                        variable
                    );
                val fieldAccess = J.FieldAccess(
                    randomId(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    method.select!!,
                    JLeftPadded(Space.EMPTY, variableExpression, Markers.EMPTY),
                    null
                )

                val assignedExpression = if (singleArgument is MapEntry) {
                    MapLiteral(
                        randomId(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        JContainer.build(listOf(JRightPadded.build(singleArgument))),
                        null
                    )
                } else
                    singleArgument

                val assignment = J.Assignment(
                    randomId(),
                    method.prefix,
                    method.markers,
                    fieldAccess,
                    JLeftPadded(Space.EMPTY, assignedExpression, Markers.EMPTY),
                    null
                )
                return autoFormat(assignment, ctx)
            }
        }
    }

//    private fun hasParentheses(method: J.MethodInvocation): Boolean {
//        // Get the arguments container (JContainer)
//        val argsContainer: JContainer<Expression> = method.arguments.getContainer()
//
//        // Check if the container's prefix starts with '(' and suffix ends with ')'
//        return argsContainer != null &&
//                argsContainer.getPrefix().getWhitespace().contains("(") &&
//                argsContainer.getSuffix().getWhitespace().contains(")")
//    }
}