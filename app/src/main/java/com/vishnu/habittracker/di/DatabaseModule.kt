package com.vishnu.habittracker.di

import android.content.Context
import androidx.room.Room
import com.vishnu.habittracker.data.local.HabitTrackerDatabase
import com.vishnu.habittracker.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt module providing Room database, all DAOs, and shared Json instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitTrackerDatabase {
        return Room.databaseBuilder(
            context,
            HabitTrackerDatabase::class.java,
            "habit_tracker_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    // ── DAO providers ────────────────────────────

    @Provides fun provideTaskDao(db: HabitTrackerDatabase): TaskDao = db.taskDao()

    @Provides fun provideHabitDao(db: HabitTrackerDatabase): HabitDao = db.habitDao()

    @Provides fun provideGoalDao(db: HabitTrackerDatabase): GoalDao = db.goalDao()

    @Provides fun provideEventDao(db: HabitTrackerDatabase): EventDao = db.eventDao()

    @Provides fun provideCalendarEventDao(db: HabitTrackerDatabase): CalendarEventDao = db.calendarEventDao()

    @Provides fun provideExpenseDao(db: HabitTrackerDatabase): ExpenseDao = db.expenseDao()

    @Provides fun provideEducationFeeDao(db: HabitTrackerDatabase): EducationFeeDao = db.educationFeeDao()

    @Provides fun provideFocusSessionDao(db: HabitTrackerDatabase): FocusSessionDao = db.focusSessionDao()
}
