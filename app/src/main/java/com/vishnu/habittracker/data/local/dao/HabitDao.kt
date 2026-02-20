package com.vishnu.habittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.vishnu.habittracker.data.local.entity.HabitEntity
import com.vishnu.habittracker.data.local.entity.HabitCompletionHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Habit DAO — CRUD operations.
 * Mirrors state.js: getHabits, addHabit, updateHabit, deleteHabit, toggleHabitToday
 */
@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE user_id = :userId ORDER BY created_at DESC")
    fun getHabitsByUser(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): HabitEntity?

    @Upsert
    suspend fun upsert(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: String)

    @Query("DELETE FROM habits WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    // ── Habit Completion History ─────────────────
    @Query("SELECT * FROM habit_completion_history WHERE user_id = :userId ORDER BY date_key DESC")
    fun getHabitHistory(userId: String): Flow<List<HabitCompletionHistoryEntity>>

    @Upsert
    suspend fun upsertHabitHistory(history: HabitCompletionHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabitHistory(history: List<HabitCompletionHistoryEntity>)
}
