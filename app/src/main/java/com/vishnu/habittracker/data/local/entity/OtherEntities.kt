package com.vishnu.habittracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goal entity — maps to Supabase `goals` table.
 *
 * SQL schema: supabase-schema.sql lines 82-93
 * State.js: addGoal() line 434-440
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val title: String,
    val type: String,               // "weekly", "monthly", "custom", or goal categories
    val duration: Int = 0,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String,
    val completed: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)

/**
 * Event entity — maps to Supabase `events` table.
 *
 * SQL schema: supabase-schema.sql lines 118-125
 * State.js: addEvent() line 382-387
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    @ColumnInfo(name = "date_time") val dateTime: String,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)

/**
 * Calendar Event entity — maps to Supabase `calendar_events` table.
 *
 * SQL schema: supabase-schema.sql lines 264-274
 */
@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "event_date") val eventDate: String,
    val name: String,
    val time: String? = null,
    val link: String? = null,
    val comments: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)

/**
 * Expense entity — maps to Supabase `expenses` table.
 *
 * SQL schema: expenses-schema.sql
 * State.js: addExpense() line 649-656
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val amount: Double,
    val category: String,           // "food_outing", "clothing", "transport", "essentials"
    val date: String,
    val description: String? = null,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)

/**
 * Education Fee entity — maps to Supabase `education_fees` table.
 *
 * State.js: updateEducationFee() line 724-746
 */
@Entity(tableName = "education_fees")
data class EducationFeeEntity(
    @PrimaryKey val id: String,     // Generated: "fee_{userId}_{semester}"
    @ColumnInfo(name = "user_id") val userId: String,
    val semester: Int,
    @ColumnInfo(name = "tuition_fee") val tuitionFee: Double = 0.0,
    @ColumnInfo(name = "hostel_fee") val hostelFee: Double = 0.0,
    @ColumnInfo(name = "tuition_paid") val tuitionPaid: Boolean = false,
    @ColumnInfo(name = "hostel_paid") val hostelPaid: Boolean = false,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)

/**
 * Focus Session entity — maps to Supabase `focus_sessions` table.
 *
 * From focus-mode.js cross-device sync
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "task_title") val taskTitle: String,
    @ColumnInfo(name = "total_seconds") val totalSeconds: Int,
    @ColumnInfo(name = "remaining_seconds") val remainingSeconds: Int,
    @ColumnInfo(name = "is_break") val isBreak: Boolean = false,
    @ColumnInfo(name = "is_paused") val isPaused: Boolean = false,
    @ColumnInfo(name = "current_subtask_index") val currentSubtaskIndex: Int = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null
)

/**
 * Task Completion History — persists even after task deletion (for analytics).
 * Composite key: (userId, taskId, dateKey)
 */
@Entity(
    tableName = "task_completion_history",
    primaryKeys = ["user_id", "task_id", "date_key"]
)
data class TaskCompletionHistoryEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    val title: String,
    val block: String,
    val priority: String,
    @ColumnInfo(name = "completed_at") val completedAt: String,
    @ColumnInfo(name = "date_key") val dateKey: String
)

/**
 * Habit Completion History — persists even after habit deletion (for analytics).
 */
@Entity(
    tableName = "habit_completion_history",
    primaryKeys = ["user_id", "habit_id", "date_key"]
)
data class HabitCompletionHistoryEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "habit_id") val habitId: String,
    val name: String,
    @ColumnInfo(name = "date_key") val dateKey: String,
    @ColumnInfo(name = "completed_at") val completedAt: String
)

/**
 * Goal Completion History — persists even after goal deletion.
 */
@Entity(
    tableName = "goal_completion_history",
    primaryKeys = ["user_id", "goal_id"]
)
data class GoalCompletionHistoryEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "goal_id") val goalId: String,
    val title: String,
    @ColumnInfo(name = "date_key") val dateKey: String,
    @ColumnInfo(name = "completed_at") val completedAt: String
)
