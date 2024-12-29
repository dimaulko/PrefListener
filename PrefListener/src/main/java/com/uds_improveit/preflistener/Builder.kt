package com.uds_improveit.preflistener

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

data class Builder(
    private val source: SOURCE = SOURCE.SHARED_PREFERENCES,
    private val deviceId: String = "",
    private val projectName: String = "",
    private val reConnection: Boolean = false,
    private val isFirstMessage: Boolean = false,
    private val sourceName: String = ""
) {

    private companion object {
        const val VERSION = 1
    }

    private val list = ArrayList<JsonElement>()

    private fun addFieldToArray(
        name: String,
        obj: Any?,
        fieldType: FieldType,
        deleted: Boolean = false
    ) {
        list.add(
            Json.encodeToJsonElement(
                UpdatesObject.PrefField(
                    name, obj.toString(), fieldType, fieldType == FieldType.NULL
                )
            )
        )
    }

    fun putValue(name: String, obj: Any?, objType: FieldType): Builder {
        addFieldToArray(name, obj, objType)
        return this
    }

    fun putValue(name: String, obj: Any?): Builder {

        when (obj) {
            null -> {
                addFieldToArray(name, obj, FieldType.NULL)
            }

            is Int -> {
                addFieldToArray(name, obj.toString(), FieldType.INT)
            }

            is Long -> {
                addFieldToArray(name, obj.toString(), FieldType.LONG)
            }

            is Float -> {
                addFieldToArray(name, obj.toString(), FieldType.FLOAT)
            }

            is Double -> {
                addFieldToArray(name, obj.toString(), FieldType.DOUBLE)
            }

            is String -> {
                addFieldToArray(name, obj.toString(), FieldType.STRING)
            }

            is Boolean -> {
                addFieldToArray(name, obj.toString(), FieldType.BOOLEAN)
            }

            else -> {
                addFieldToArray(name, obj.toString(), FieldType.UNDEFINED)
            }
        }
        return this
    }

    fun build(): UpdatesObject {
        return UpdatesObject(
            deviceId = deviceId,
            source = source,
            data = JsonArray(list),
            project = projectName,
            sourceName = sourceName,
            timestamp = System.currentTimeMillis(),
            version = VERSION,
            reConnection = reConnection
        )
    }

    @Serializable
    data class UpdatesObject(
        @SerialName("deviceId") var deviceId: String = "",
        val source: SOURCE,
        @SerialName("project") val project: String,
        @SerialName("sourceName") val sourceName: String,
        @SerialName("data") val data: JsonArray,
        @SerialName("t") val timestamp: Long,
        @SerialName("ver") val version: Int,
        @SerialName("reConnection") val reConnection: Boolean
    ) {
        @Serializable
        data class PrefField(
            @SerialName("k") val keyName: String,
            @SerialName("v") val value: String?,
            @SerialName("vt") val valueType: FieldType,
            @SerialName("d") val deleted: Boolean,
        )
    }

    enum class SOURCE(code: Int) {
        SHARED_PREFERENCES(0), DATASTORE(1)
    }

    enum class FieldType {
        BOOLEAN, INT, LONG, FLOAT, DOUBLE, STRING, NULL, UNDEFINED
    }
}
