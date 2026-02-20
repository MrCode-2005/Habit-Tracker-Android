package com.vishnu.habittracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.GoalEntity
import com.vishnu.habittracker.data.local.entity.HabitEntity
import com.vishnu.habittracker.data.local.entity.TaskEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.GoalRepository
import com.vishnu.habittracker.data.repository.HabitRepository
import com.vishnu.habittracker.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class ProductivityStats(
    val completedTasks: Int = 0,
    val taskStreak: Int = 0,
    val mostProductiveDay: String = "-",
    val habitsCompletedToday: Int = 0,
    val totalHabits: Int = 0,
    val goalsCompleted: Int = 0,
    val goalsActive: Int = 0,
    val goalsFailed: Int = 0,
    val weeklyTaskData: List<Pair<String, Int>> = emptyList(),  // label to %
    val weeklyHabitData: List<Pair<String, Int>> = emptyList()
)

/**
 * AnalyticsViewModel — computes productivity stats and chart data.
 * Port of: analytics.js (1,234 lines) — getWeeklyData, getTaskStreak, getMostProductiveDay, renderProductivityStats
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.getTasks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = habitRepository.getHabits(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = goalRepository.getGoals(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun computeStats(tasks: List<TaskEntity>, habits: List<HabitEntity>, goals: List<GoalEntity>): ProductivityStats {
        val today = LocalDate.now().toString()

        // Task stats
        val completedTasks = tasks.count { it.completed }

        // Most productive day — port of getMostProductiveDay()
        val dayCounts = IntArray(7)
        tasks.filter { it.completed }.forEach { task ->
            try {
                val date = if (task.completedAt != null) {
                    Instant.parse(task.completedAt).atZone(ZoneId.systemDefault()).toLocalDate()
                } else null
                date?.dayOfWeek?.let { dayCounts[it.value % 7]++ }
            } catch (_: Exception) {}
        }
        val maxDayCount = dayCounts.max()
        val mostProductiveDay = if (maxDayCount > 0) {
            val idx = dayCounts.toList().indexOf(maxDayCount)
            DayOfWeek.of(if (idx == 0) 7 else idx).getDisplayName(TextStyle.FULL, Locale.getDefault())
        } else "-"

        // Task streak — port of getTaskStreak()
        val completionDates = mutableSetOf<String>()
        tasks.filter { it.completed }.forEach { task ->
            try {
                val dateKey = if (task.completedAt != null)
                    Instant.parse(task.completedAt).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                else null
                dateKey?.let { completionDates.add(it) }
            } catch (_: Exception) {}
        }
        val taskStreak = calculateStreak(completionDates)

        // Habit stats
        val habitsCompletedToday = habits.count { it.completions[today] == true }

        // Goal stats
        val now = LocalDate.now()
        var goalsCompleted = 0; var goalsActive = 0; var goalsFailed = 0
        goals.forEach { goal ->
            when {
                goal.completed -> goalsCompleted++
                try { LocalDate.parse(goal.endDate.take(10)).isBefore(now) } catch (_: Exception) { false } -> goalsFailed++
                else -> goalsActive++
            }
        }

        // Weekly task data (last 7 days) — port of getWeeklyData()
        val weeklyTaskData = (6 downTo 0).map { i ->
            val date = LocalDate.now().minusDays(i.toLong())
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val dateKey = date.toString()
            val count = tasks.count { t ->
                t.completed && try {
                    t.completedAt?.let { Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDate().toString() == dateKey } ?: false
                } catch (_: Exception) { false }
            }
            dayLabel to minOf(100, count * 20)
        }

        // Weekly habit data (last 7 days)
        val weeklyHabitData = (6 downTo 0).map { i ->
            val date = LocalDate.now().minusDays(i.toLong())
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val dateKey = date.toString()
            val completed = habits.count { it.completions[dateKey] == true }
            val rate = if (habits.isNotEmpty()) minOf(100, completed * 100 / habits.size) else 0
            dayLabel to rate
        }

        return ProductivityStats(
            completedTasks = completedTasks,
            taskStreak = taskStreak,
            mostProductiveDay = mostProductiveDay,
            habitsCompletedToday = habitsCompletedToday,
            totalHabits = habits.size,
            goalsCompleted = goalsCompleted,
            goalsActive = goalsActive,
            goalsFailed = goalsFailed,
            weeklyTaskData = weeklyTaskData,
            weeklyHabitData = weeklyHabitData
        )
    }

    private fun calculateStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        if (today !in dates && yesterday !in dates) return 0

        var streak = 0
        var checkDate = if (today in dates) LocalDate.now() else LocalDate.now().minusDays(1)
        while (checkDate.toString() in dates) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }
}
