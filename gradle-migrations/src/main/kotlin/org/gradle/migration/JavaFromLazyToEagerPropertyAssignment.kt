package org.gradle.migration

import com.fasterxml.jackson.annotation.JsonProperty
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.java.JavaTemplate
import org.openrewrite.java.JavaVisitor
import org.openrewrite.java.MethodMatcher
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType
import org.openrewrite.java.tree.TypeUtils

/**
 * Inserts `.get()` after a lazy-property getter call when the consuming context still
 * expects the eager value (variable initializer, method argument, chained eager-method, or
 * `return` whose enclosing method's return type isn't a `Provider`).
 *
 * **Languages:** Java and Kotlin. Like `[JavaConvertToLazyProperty]`, this recipe extends
 * `JavaVisitor` and therefore matches any `J.MethodInvocation` produced by the Java *or*
 * Kotlin parsers — both call shapes (`t.getX()` in Java, the same in Kotlin source) flow
 * through the same node. Idiomatic Kotlin property *reads* (`t.x` rather than `t.getX()`)
 * are not matched; that's a separate rewrite.
 */
@Suppress("unused")
class JavaFromLazyToEagerPropertyAssignment(
    @JsonProperty("targetType") val targetType: String,
    @JsonProperty("propertyAccessor") val propertyAccessor: String,
) : Recipe() {
    override fun getDisplayName(): String =
        "Inserts .get() when reading lazy property $propertyAccessor on $targetType"

    override fun getDescription(): String = "$displayName."

    private val matcher: MethodMatcher get() = MethodMatcher("$targetType $propertyAccessor()")

    override fun getVisitor(): JavaVisitor<ExecutionContext> =
        object : JavaVisitor<ExecutionContext>() {
            override fun visitMethodInvocation(method: J.MethodInvocation, ctx: ExecutionContext): J {
                val visited = super.visitMethodInvocation(method, ctx) as J.MethodInvocation
                if (!matcher.matches(visited)) {
                    return visited
                }
                if (isAlreadyWrappedByGet()) {
                    return visited
                }
                if (!consumerExpectsEagerValue()) {
                    return visited
                }
                return JavaTemplate.builder("#{any()}.get()")
                    .build()
                    .apply(cursor, visited.coordinates.replace(), visited)
            }

            private fun isAlreadyWrappedByGet(): Boolean {
                val parent = cursor.parentTreeCursor.getValue<Any>()
                return parent is J.MethodInvocation
                        && parent.simpleName == "get"
                        && parent.select === cursor.getValue<Any>()
            }

            private fun consumerExpectsEagerValue(): Boolean {
                val current = cursor.getValue<Any>()
                val parent = cursor.parentTreeCursor.getValue<Any>()
                return when (parent) {
                    is J.VariableDeclarations.NamedVariable ->
                        !isLazyType(parent.variableType?.type)
                    is J.MethodInvocation ->
                        if (parent.select === current) {
                            // Chain: the next method is invoked on our result. If it is
                            // declared on Provider/Property, the chain stays lazy and we
                            // must not wrap. If it is declared on the eager type
                            // (e.g. URI.toASCIIString), wrap.
                            !isLazyType(parent.methodType?.declaringType)
                        } else {
                            val argIndex = parent.arguments.indexOfFirst { it === current }
                            !isLazyType(paramTypeAt(parent, argIndex))
                        }
                    is J.Return -> {
                        val enclosing = cursor.firstEnclosing(J.MethodDeclaration::class.java)
                        val returnType = enclosing?.methodType?.returnType
                            ?: enclosing?.returnTypeExpression?.type
                        !isLazyType(returnType)
                    }
                    else -> true
                }
            }

            private fun paramTypeAt(call: J.MethodInvocation, argIndex: Int): JavaType? {
                call.methodType?.parameterTypes?.getOrNull(argIndex)?.let { return it }
                // Fallback: when type validation is relaxed and the call's methodType
                // is unresolved, recover the parameter type from the method declaration
                // in the enclosing class.
                val enclosingClass = cursor.firstEnclosing(J.ClassDeclaration::class.java) ?: return null
                val methodDecl = enclosingClass.body.statements
                    .filterIsInstance<J.MethodDeclaration>()
                    .firstOrNull { it.simpleName == call.simpleName && it.parameters.size == call.arguments.size }
                    ?: return null
                val paramDecl = methodDecl.parameters.getOrNull(argIndex) as? J.VariableDeclarations ?: return null
                return paramDecl.typeExpression?.type
            }

            private fun isLazyType(type: JavaType?): Boolean =
                TypeUtils.isAssignableTo("org.gradle.api.provider.Provider", type)
        }
}
