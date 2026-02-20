package com.vishnu.habittracker.data.local

import androidx.room.TypeConverter
import com.vishnu.habittracker.data.local.entity.Subtask
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for complex types (JSONB fields in Supabase).
 *
 * Converts between Kotlin objects and JSON strings for Room storage:
 * - List<Subtask> ↔ JSON string (for tasks.subtasks)
 * - Map<String, Boolean> ↔ JSON string (for habits.completions)
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ── Subtask List ──────────────────────────────
    @TypeConverter
    fun fromSubtaskList(subtasks: List<Subtask>): String {
        return json.encodeToString(subtasks)
    }

    @TypeConverter
    fun toSubtaskList(data: String): List<Subtask> {
        return try {
            json.decodeFromString(data)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Completions Map ───────────────────────────
    @TypeConverter
    fun fromCompletionsMap(completions: Map<String, Boolean>): String {
        return json.encodeToString(completions)
    }

    @TypeConverter
    fun toCompletionsMap(data: String): Map<String, Boolean> {
        return try {
            json.decodeFromString(data)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
