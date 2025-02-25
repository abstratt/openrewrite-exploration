package org.gradle.migration

import com.fasterxml.jackson.annotation.JsonProperty
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.SourceFile
import org.openrewrite.java.JavaVisitor
import org.openrewrite.java.MethodMatcher
import org.openrewrite.java.tree.Expression
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.J.MethodInvocation


@Suppress("unused")
public class JavaConvertToLazyProperty constructor(@JsonProperty("oldPattern")  val oldPattern: String, @JsonProperty("newPropertyGetter") val newPropertyGetter: String, @JsonProperty("newPropertySetter") val newPropertySetter: String) : Recipe() {
    override fun getDisplayName(): String = "Converts eager property ${oldPattern} to lazy property usage $newPropertyGetter"
    override fun getDescription(): String = "${displayName}."

    private val methodMatcher: MethodMatcher get() = MethodMatcher(oldPattern)

    override fun getVisitor(): JavaVisitor<ExecutionContext> {
        return object : JavaVisitor<ExecutionContext>() {
            override fun visitMethodInvocation(method: MethodInvocation, p: ExecutionContext): J {
                val method: MethodInvocation = super.visitMethodInvocation(method, p) as MethodInvocation
                if (methodMatcher.matches(method)) {
                    println("*** Modifying ${cursor.firstEnclosing(SourceFile::class.java)!!.sourcePath}")

                    // Extract the argument (boolean b)
                    val argument: Expression = method.arguments[0]

                    // Transform `setIncremental(b)` → `getIncremental()`
                    val getIncrementalCall = method
                        .withName(method.name.withSimpleName(newPropertyGetter)) // Rename method
                        .withArguments(emptyList()) // Remove arguments

                    // Transform `setIncremental(b)` → `getIncremental().set(b)`
                    val modified = method
                        .withSelect(getIncrementalCall) // Change receiver to `getIncremental()`
                        .withName(method.name.withSimpleName(newPropertySetter)) // Rename to `set`
                        .withArguments(listOf(argument)) // Keep `b`

                    println("*** Refactored $method into $modified")
                    return modified
                }
                return method
            }
        }
    }
}