package com.vishnu.habittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vishnu.habittracker.data.local.dao.*
import com.vishnu.habittracker.data.local.entity.*

/**
 * Room database for the Habit Tracker app.
 * All 11 tables covering every feature from the webapp.
 */
@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        GoalEntity::class,
        EventEntity::class,
        CalendarEventEntity::class,
        ExpenseEntity::class,
        EducationFeeEntity::class,
        FocusSessionEntity::class,
        TaskCompletionHistoryEntity::class,
        HabitCompletionHistoryEntity::class,
        GoalCompletionHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun eventDao(): EventDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun educationFeeDao(): EducationFeeDao
    abstract fun focusSessionDao(): FocusSessionDao
}
