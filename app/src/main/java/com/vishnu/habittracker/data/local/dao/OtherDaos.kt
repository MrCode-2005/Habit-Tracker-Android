package com.vishnu.habittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.vishnu.habittracker.data.local.entity.GoalEntity
import com.vishnu.habittracker.data.local.entity.GoalCompletionHistoryEntity
import com.vishnu.habittracker.data.local.entity.EventEntity
import com.vishnu.habittracker.data.local.entity.CalendarEventEntity
import com.vishnu.habittracker.data.local.entity.ExpenseEntity
import com.vishnu.habittracker.data.local.entity.EducationFeeEntity
import com.vishnu.habittracker.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

// ─── Goal DAO ────────────────────────────────────────

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY end_date ASC")
    fun getGoalsByUser(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): GoalEntity?

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: String)

    @Query("DELETE FROM goals WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    // Goal history
    @Query("SELECT * FROM goal_completion_history WHERE user_id = :userId")
    fun getGoalHistory(userId: String): Flow<List<GoalCompletionHistoryEntity>>

    @Upsert
    suspend fun upsertGoalHistory(history: GoalCompletionHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoalHistory(history: List<GoalCompletionHistoryEntity>)
}

// ─── Event DAO ───────────────────────────────────────

@Dao
interface EventDao {

    @Query("SELECT * FROM events WHERE user_id = :userId ORDER BY date_time ASC")
    fun getEventsByUser(userId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Upsert
    suspend fun upsert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM events WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

// ─── Calendar Event DAO ──────────────────────────────

@Dao
interface CalendarEventDao {

    @Query("SELECT * FROM calendar_events WHERE user_id = :userId ORDER BY event_date ASC")
    fun getCalendarEventsByUser(userId: String): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE user_id = :userId AND event_date = :date")
    fun getCalendarEventsByDate(userId: String, date: String): Flow<List<CalendarEventEntity>>

    @Upsert
    suspend fun upsert(event: CalendarEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteCalendarEvent(eventId: String)

    @Query("DELETE FROM calendar_events WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

// ─── Expense DAO ─────────────────────────────────────

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE user_id = :userId AND is_deleted = 0 ORDER BY date DESC")
    fun getActiveExpenses(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE user_id = :userId AND category = :category AND is_deleted = 0 ORDER BY date DESC")
    fun getExpensesByCategory(userId: String, category: String): Flow<List<ExpenseEntity>>

    @Upsert
    suspend fun upsert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query("UPDATE expenses SET is_deleted = 1 WHERE id = :expenseId")
    suspend fun softDelete(expenseId: String)

    @Query("DELETE FROM expenses WHERE user_id = :userId AND is_deleted = 1")
    suspend fun clearDeletedExpenses(userId: String)

    @Query("DELETE FROM expenses WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

// ─── Education Fee DAO ───────────────────────────────

@Dao
interface EducationFeeDao {

    @Query("SELECT * FROM education_fees WHERE user_id = :userId ORDER BY semester ASC")
    fun getFeesByUser(userId: String): Flow<List<EducationFeeEntity>>

    @Upsert
    suspend fun upsert(fee: EducationFeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fees: List<EducationFeeEntity>)
}

// ─── Focus Session DAO ───────────────────────────────

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions WHERE user_id = :userId ORDER BY updated_at DESC LIMIT 1")
    suspend fun getActiveSession(userId: String): FocusSessionEntity?

    @Upsert
    suspend fun upsert(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM focus_sessions WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}
