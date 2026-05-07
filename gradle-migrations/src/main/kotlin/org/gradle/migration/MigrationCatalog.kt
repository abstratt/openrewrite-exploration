package org.gradle.migration

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

data class MigrationEntry @JsonCreator constructor(
    @JsonProperty("class") val cls: String,
    @JsonProperty("property") val property: String,
    @JsonProperty("old_type") val oldType: String,
    @JsonProperty("new_type") val newType: String,
    @JsonProperty("kind") val kind: String,
    @JsonProperty("new_is_provider") val newIsProvider: Boolean,
    @JsonProperty("new_read_accessor") val newReadAccessor: String?,
    @JsonProperty("new_write_accessor") val newWriteAccessor: String?,
    @JsonProperty("removed_accessors") val removedAccessors: List<String>,
    @JsonProperty("changed_return_accessors") val changedReturnAccessors: List<String>,
    @JsonProperty("inheriting_subtypes") val inheritingSubtypes: List<String>,
)

object MigrationCatalog {
    val entries: List<MigrationEntry> by lazy { load() }

    private fun load(): List<MigrationEntry> {
        val stream = MigrationCatalog::class.java.classLoader
            .getResourceAsStream("migration-data.json")
            ?: error("migration-data.json missing from classpath")
        val mapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return stream.use {
            mapper.readValue(it, object : TypeReference<List<MigrationEntry>>() {})
        }
    }
}
