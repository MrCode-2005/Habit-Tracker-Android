package com.vishnu.habittracker.data.repository

import com.vishnu.habittracker.data.local.dao.HabitDao
import com.vishnu.habittracker.data.local.entity.HabitCompletionHistoryEntity
import com.vishnu.habittracker.data.local.entity.HabitEntity
import com.vishnu.habittracker.util.DateUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Habit Repository — local-first with Supabase sync.
 * Mirrors: state.js habits CRUD + calculateStreak() (lines 236-366, 569-628)
 */
@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val supabaseClient: SupabaseClient
) {
    fun getHabits(userId: String): Flow<List<HabitEntity>> = habitDao.getHabitsByUser(userId)

    fun getHabitHistory(userId: String): Flow<List<HabitCompletionHistoryEntity>> =
        habitDao.getHabitHistory(userId)

    suspend fun addHabit(userId: String, name: String): HabitEntity {
        val habit = HabitEntity(
            id = DateUtils.generateId(),
            userId = userId,
            name = name,
            completions = emptyMap(),
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        habitDao.upsert(habit)
        syncHabitToSupabase(habit)
        return habit
    }

    suspend fun updateHabit(habit: HabitEntity) {
        val updated = habit.copy(updatedAt = java.time.Instant.now().toString())
        habitDao.upsert(updated)
        syncHabitToSupabase(updated)
    }

    /** Toggle today's completion (mirrors state.js toggleHabitToday) */
    suspend fun toggleHabitToday(habitId: String): HabitEntity? {
        val habit = habitDao.getHabitById(habitId) ?: return null
        val todayKey = DateUtils.todayKey()
        val newCompletions = habit.completions.toMutableMap()

        if (newCompletions[todayKey] == true) {
            newCompletions.remove(todayKey)
        } else {
            newCompletions[todayKey] = true
        }

        val streaks = calculateStreak(newCompletions)
        val updated = habit.copy(
            completions = newCompletions,
            currentStreak = streaks.first,
            bestStreak = streaks.second,
            updatedAt = java.time.Instant.now().toString()
        )
        habitDao.upsert(updated)
        syncHabitToSupabase(updated)
        return updated
    }

    suspend fun deleteHabit(habitId: String, userId: String) {
        val habit = habitDao.getHabitById(habitId)
        // Record all completions to history before deleting (mirrors state.js deleteHabit)
        if (habit != null) {
            habit.completions.forEach { (dateKey, completed) ->
                if (completed) {
                    habitDao.upsertHabitHistory(
                        HabitCompletionHistoryEntity(
                            userId = userId,
                            habitId = habit.id,
                            name = habit.name,
                            dateKey = dateKey,
                            completedAt = java.time.Instant.now().toString()
                        )
                    )
                }
            }
        }
        habitDao.deleteHabit(habitId)
        deleteHabitFromSupabase(habitId)
    }

    /**
     * Calculate current and longest streak from completions map.
     * Exact port of state.js calculateStreak() (lines 569-628).
     */
    fun calculateStreak(completions: Map<String, Boolean>): Pair<Int, Int> {
        if (completions.isEmpty()) return Pair(0, 0)

        val sortedDates = completions.keys.sorted().reversed()
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0

        val todayKey = DateUtils.todayKey()
        val yesterdayKey = DateUtils.yesterdayKey()

        // Current streak — check from today or yesterday backwards
        if (completions[todayKey] == true || completions[yesterdayKey] == true) {
            val startKey = if (completions[todayKey] == true) todayKey else yesterdayKey
            val startDate = DateUtils.fromDateKey(startKey)
            if (startDate != null) {
                val cal = java.util.Calendar.getInstance()
                cal.time = startDate
                while (true) {
                    val checkKey = DateUtils.toDateKey(cal.time)
                    if (completions[checkKey] == true) {
                        currentStreak++
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            }
        }

        // Longest streak — iterate all dates
        var lastDate: java.util.Date? = null
        for (dateKey in sortedDates) {
            val date = DateUtils.fromDateKey(dateKey) ?: continue
            if (lastDate == null) {
                tempStreak = 1
            } else {
                val diffDays = ((lastDate.time - date.time) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    tempStreak++
                } else {
                    longestStreak = maxOf(longestStreak, tempStreak)
                    tempStreak = 1
                }
            }
            lastDate = date
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        return Pair(currentStreak, longestStreak)
    }

    suspend fun syncFromSupabase(userId: String) {
        try {
            val result = supabaseClient.postgrest["habits"]
                .select { filter { eq("user_id", userId) } }
            val habits = result.decodeList<HabitSupabaseDto>()
            habitDao.insertAll(habits.map { it.toEntity() })
        } catch (e: Exception) { /* offline */ }
    }

    suspend fun clearLocalData(userId: String) = habitDao.deleteAllForUser(userId)

    private suspend fun syncHabitToSupabase(habit: HabitEntity) {
        try {
            supabaseClient.postgrest["habits"].upsert(HabitSupabaseDto.fromEntity(habit))
        } catch (e: Exception) { /* queue */ }
    }

    private suspend fun deleteHabitFromSupabase(habitId: String) {
        try {
            supabaseClient.postgrest["habits"].delete { filter { eq("id", habitId) } }
        } catch (e: Exception) { /* queue */ }
    }
}

@Serializable
data class HabitSupabaseDto(
    val id: String,
    val user_id: String,
    val name: String,
    val completions: Map<String, Boolean> = emptyMap(),
    val current_streak: Int = 0,
    val best_streak: Int = 0,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    fun toEntity() = HabitEntity(
        id = id, userId = user_id, name = name, completions = completions,
        currentStreak = current_streak, bestStreak = best_streak,
        createdAt = created_at, updatedAt = updated_at
    )
    companion object {
        fun fromEntity(e: HabitEntity) = HabitSupabaseDto(
            id = e.id, user_id = e.userId, name = e.name, completions = e.completions,
            current_streak = e.currentStreak, best_streak = e.bestStreak,
            created_at = e.createdAt, updated_at = e.updatedAt
        )
    }
}
