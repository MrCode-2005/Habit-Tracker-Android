package com.vishnu.habittracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Subtask model — supports recursive nesting (children).
 * Mirrors webapp's task subtask structure from state.js / tasks.js.
 */
@Serializable
data class Subtask(
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val duration: Int = 15,      // minutes
    val link: String = "",
    val comment: String = "",
    val children: List<Subtask> = emptyList()
)

/**
 * Task entity — maps to Supabase `tasks` table.
 *
 * SQL schema: supabase-schema.sql lines 10-23
 * State.js: addTask() line 73-81, fields: id, title, block, priority, hours, minutes,
 *           notes, subtasks, completed, createdAt, completedAt
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val title: String,
    val block: String,              // "morning", "evening", "night"
    val priority: String,           // "IAP", "IBNU", "NIBU", "NINU"
    val hours: Int = 0,
    val minutes: Int = 0,
    val notes: String? = null,
    val subtasks: List<Subtask> = emptyList(),  // JSONB → TypeConverter
    val completed: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)
