package com.vishnu.habittracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Habit entity — maps to Supabase `habits` table.
 *
 * SQL schema: supabase-schema.sql lines 48-57
 * State.js: addHabit() line 241-248, completions = { "YYYY-MM-DD": true }
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    val completions: Map<String, Boolean> = emptyMap(), // JSONB → TypeConverter
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "best_streak") val bestStreak: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)
