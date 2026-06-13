package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.util.Log

object JsonSerializer {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun serializeSections(sections: List<ExamSection>): String {
        return try {
            val type = Types.newParameterizedType(List::class.java, ExamSection::class.java)
            val adapter = moshi.adapter<List<ExamSection>>(type)
            adapter.toJson(sections)
        } catch (e: Exception) {
            Log.e("JsonSerializer", "Error serializing sections", e)
            "[]"
        }
    }

    fun deserializeSections(json: String): List<ExamSection> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, ExamSection::class.java)
            val adapter = moshi.adapter<List<ExamSection>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("JsonSerializer", "Error deserializing sections: $json", e)
            emptyList()
        }
    }

    fun serializeMap(map: Map<Int, Int>): String {
        return try {
            val type = Types.newParameterizedType(Map::class.java, Integer::class.java, Integer::class.java)
            // Convert to Map<String, Int> since JSON keys must be Strings, or use a custom adapter.
            // Converting Int keys to Strings is safer for standard JSON serializers.
            val stringKeyMap = map.mapKeys { it.key.toString() }
            val stringMapType = Types.newParameterizedType(Map::class.java, String::class.java, Integer::class.java)
            val adapter = moshi.adapter<Map<String, Int>>(stringMapType)
            adapter.toJson(stringKeyMap)
        } catch (e: Exception) {
            Log.e("JsonSerializer", "Error serializing map", e)
            "{}"
        }
    }

    fun deserializeMap(json: String): Map<Int, Int> {
        if (json.isBlank() || json == "{}") return emptyMap()
        return try {
            val stringMapType = Types.newParameterizedType(Map::class.java, String::class.java, Integer::class.java)
            val adapter = moshi.adapter<Map<String, Int>>(stringMapType)
            val stringKeyMap = adapter.fromJson(json) ?: emptyMap()
            stringKeyMap.mapKeys { it.key.toIntOrNull() ?: 0 }.filterKeys { it != 0 }
        } catch (e: Exception) {
            Log.e("JsonSerializer", "Error deserializing map: $json", e)
            emptyMap()
        }
    }
}
