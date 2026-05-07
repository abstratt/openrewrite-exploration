package org.gradle.migration

import com.fasterxml.jackson.annotation.JsonProperty
import org.openrewrite.Recipe

/**
 * Reads `migration-data.json` at runtime and dynamically builds a `recipeList` of pre-
 * parameterised children of `target`. Lets the aggregate `Gradle9to10` recipe stay tiny
 * (one block per recipe class) while still covering every catalogue entry.
 *
 * **Languages:** inherited from the target recipe. See the docs on
 * `[JavaConvertToLazyProperty]` (Java + Kotlin),
 * `[JavaFromLazyToEagerPropertyAssignment]` (Java + Kotlin), and
 * `[KotlinAddOperatorImports]` (Kotlin only).
 */
@Suppress("unused")
class MigrationCompositeRecipe(
    @JsonProperty("target") val target: String,
) : Recipe() {
    override fun getDisplayName(): String = "Migration composite for $target"
    override fun getDescription(): String = "$displayName."

    override fun getRecipeList(): List<Recipe> = buildChildren()

    private fun buildChildren(): List<Recipe> {
        val entries = MigrationCatalog.entries
        return when (target) {
            JavaConvertToLazyProperty::class.java.name ->
                entries.flatMap { e ->
                    e.removedAccessors
                        .filter { it.startsWith("set") && it.contains("(") }
                        .map { setter ->
                            JavaConvertToLazyProperty(
                                oldPattern = "*..* ${stripGenerics(setter)}",
                                newPropertyGetter = "get${e.property.cap()}",
                                newPropertySetter =
                                    if (e.newType.startsWith("org.gradle.api.file.ConfigurableFileCollection")) "setFrom"
                                    else "set",
                                targetType = e.cls,
                            )
                        }
                }
            KotlinAddOperatorImports::class.java.name ->
                entries.filter { !it.newIsProvider }.map {
                    KotlinAddOperatorImports(
                        targetType = it.cls,
                        propertyName = it.property,
                    )
                }
            JavaFromLazyToEagerPropertyAssignment::class.java.name ->
                entries
                    .filter { !isAlreadyLazyForReadSide(it.oldType) }
                    .flatMap { e ->
                        (listOf(e.cls) + e.inheritingSubtypes).map { type ->
                            JavaFromLazyToEagerPropertyAssignment(
                                targetType = type,
                                propertyAccessor = "get${e.property.cap()}",
                            )
                        }
                    }
            else -> error("Unsupported target recipe: $target")
        }
    }
}

private fun String.cap(): String =
    if (isEmpty()) this else this[0].uppercaseChar() + substring(1)

// Read-side .get() insertion makes no sense when the old read accessor never
// returned an eager value. Two distinct families qualify:
//   1. Provider/Property family — already lazy: callers were already consuming
//      a Provider-shaped value, so wrapping with .get() is gratuitous (and
//      breaks chains like `someProperty.set(...)` or `someProvider.flatMap(...)`).
//   2. FileCollection / ConfigurableFileCollection — neither has a `.get()`
//      accessor. FileCollection is the eager-but-iterable pre-migration
//      shape; ConfigurableFileCollection is the lazy-ish post-migration
//      shape. They're treated separately because the *reason* differs, but
//      the behaviour is the same: no read-side rewrite applies.
private fun isAlreadyLazyForReadSide(oldType: String): Boolean {
    // 1. Provider/Property family
    if (oldType.startsWith("org.gradle.api.provider.")) return true
    if (oldType.startsWith("org.gradle.api.file.RegularFileProperty")) return true
    if (oldType.startsWith("org.gradle.api.file.DirectoryProperty")) return true
    // 2a. FileCollection — eager but no .get() accessor
    if (oldType.startsWith("org.gradle.api.file.FileCollection")) return true
    // 2b. ConfigurableFileCollection — lazy-ish, also no .get() accessor
    if (oldType.startsWith("org.gradle.api.file.ConfigurableFileCollection")) return true
    return false
}

// Erased type names — MethodMatcher patterns don't carry generics.
private fun stripGenerics(signature: String): String {
    val sb = StringBuilder()
    var depth = 0
    for (c in signature) {
        when (c) {
            '<' -> depth++
            '>' -> if (depth > 0) depth--
            else -> if (depth == 0) sb.append(c)
        }
    }
    return sb.toString()
}
