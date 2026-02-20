package com.vishnu.habittracker.data.repository

import com.vishnu.habittracker.data.local.dao.*
import com.vishnu.habittracker.data.local.entity.*
import com.vishnu.habittracker.util.DateUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════
// Goal Repository
// ═══════════════════════════════════════════════

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val supabaseClient: SupabaseClient
) {
    fun getGoals(userId: String): Flow<List<GoalEntity>> = goalDao.getGoalsByUser(userId)
    fun getGoalHistory(userId: String): Flow<List<GoalCompletionHistoryEntity>> = goalDao.getGoalHistory(userId)

    suspend fun addGoal(userId: String, title: String, type: String,
                        duration: Int, startDate: String, endDate: String): GoalEntity {
        val goal = GoalEntity(
            id = DateUtils.generateId(), userId = userId, title = title,
            type = type, duration = duration, startDate = startDate, endDate = endDate,
            createdAt = java.time.Instant.now().toString(), updatedAt = java.time.Instant.now().toString()
        )
        goalDao.upsert(goal)
        try { supabaseClient.postgrest["goals"].upsert(GoalDto.fromEntity(goal)) } catch (_: Exception) {}
        return goal
    }

    suspend fun updateGoal(goal: GoalEntity) {
        val updated = goal.copy(updatedAt = java.time.Instant.now().toString())
        goalDao.upsert(updated)
        try { supabaseClient.postgrest["goals"].upsert(GoalDto.fromEntity(updated)) } catch (_: Exception) {}
    }

    suspend fun toggleGoalComplete(goalId: String): GoalEntity? {
        val goal = goalDao.getGoalById(goalId) ?: return null
        val updated = goal.copy(completed = !goal.completed, updatedAt = java.time.Instant.now().toString())
        goalDao.upsert(updated)
        try { supabaseClient.postgrest["goals"].upsert(GoalDto.fromEntity(updated)) } catch (_: Exception) {}
        return updated
    }

    suspend fun deleteGoal(goalId: String, userId: String) {
        val goal = goalDao.getGoalById(goalId)
        if (goal?.completed == true) {
            goalDao.upsertGoalHistory(GoalCompletionHistoryEntity(
                userId = userId, goalId = goal.id, title = goal.title,
                dateKey = DateUtils.todayKey(), completedAt = java.time.Instant.now().toString()
            ))
        }
        goalDao.deleteGoal(goalId)
        try { supabaseClient.postgrest["goals"].delete { filter { eq("id", goalId) } } } catch (_: Exception) {}
    }

    suspend fun syncFromSupabase(userId: String) {
        try {
            val goals = supabaseClient.postgrest["goals"].select { filter { eq("user_id", userId) } }.decodeList<GoalDto>()
            goalDao.insertAll(goals.map { it.toEntity() })
        } catch (_: Exception) {}
    }

    suspend fun clearLocalData(userId: String) = goalDao.deleteAllForUser(userId)
}

@Serializable data class GoalDto(
    val id: String, val user_id: String, val title: String, val type: String,
    val duration: Int = 0, val start_date: String, val end_date: String,
    val completed: Boolean = false, val created_at: String? = null, val updated_at: String? = null
) {
    fun toEntity() = GoalEntity(id, user_id, title, type, duration, start_date, end_date, completed, created_at, updated_at)
    companion object { fun fromEntity(e: GoalEntity) = GoalDto(e.id, e.userId, e.title, e.type, e.duration, e.startDate, e.endDate, e.completed, e.createdAt, e.updatedAt) }
}

// ═══════════════════════════════════════════════
// Event Repository
// ═══════════════════════════════════════════════

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val supabaseClient: SupabaseClient
) {
    fun getEvents(userId: String): Flow<List<EventEntity>> = eventDao.getEventsByUser(userId)

    suspend fun addEvent(userId: String, name: String, dateTime: String): EventEntity {
        val event = EventEntity(
            id = DateUtils.generateId(), userId = userId, name = name, dateTime = dateTime,
            createdAt = java.time.Instant.now().toString(), updatedAt = java.time.Instant.now().toString()
        )
        eventDao.upsert(event)
        try { supabaseClient.postgrest["events"].upsert(EventDto.fromEntity(event)) } catch (_: Exception) {}
        return event
    }

    suspend fun updateEvent(event: EventEntity) {
        val updated = event.copy(updatedAt = java.time.Instant.now().toString())
        eventDao.upsert(updated)
        try { supabaseClient.postgrest["events"].upsert(EventDto.fromEntity(updated)) } catch (_: Exception) {}
    }

    suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEvent(eventId)
        try { supabaseClient.postgrest["events"].delete { filter { eq("id", eventId) } } } catch (_: Exception) {}
    }

    suspend fun syncFromSupabase(userId: String) {
        try {
            val events = supabaseClient.postgrest["events"].select { filter { eq("user_id", userId) } }.decodeList<EventDto>()
            eventDao.insertAll(events.map { it.toEntity() })
        } catch (_: Exception) {}
    }

    suspend fun clearLocalData(userId: String) = eventDao.deleteAllForUser(userId)
}

@Serializable data class EventDto(
    val id: String, val user_id: String, val name: String, val date_time: String,
    val created_at: String? = null, val updated_at: String? = null
) {
    fun toEntity() = EventEntity(id, user_id, name, date_time, created_at, updated_at)
    companion object { fun fromEntity(e: EventEntity) = EventDto(e.id, e.userId, e.name, e.dateTime, e.createdAt, e.updatedAt) }
}

// ═══════════════════════════════════════════════
// Expense Repository
// ═══════════════════════════════════════════════

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val supabaseClient: SupabaseClient
) {
    fun getActiveExpenses(userId: String): Flow<List<ExpenseEntity>> = expenseDao.getActiveExpenses(userId)
    fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses(userId)
    fun getExpensesByCategory(userId: String, category: String): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByCategory(userId, category)

    suspend fun addExpense(userId: String, amount: Double, category: String,
                           date: String, description: String?): ExpenseEntity {
        val expense = ExpenseEntity(
            id = "exp_${System.currentTimeMillis()}_${(Math.random() * 1000000).toInt()}",
            userId = userId, amount = amount, category = category,
            date = date, description = description,
            createdAt = java.time.Instant.now().toString(), updatedAt = java.time.Instant.now().toString()
        )
        expenseDao.upsert(expense)
        try { supabaseClient.postgrest["expenses"].upsert(ExpenseDto.fromEntity(expense)) } catch (_: Exception) {}
        return expense
    }

    suspend fun softDeleteExpense(expenseId: String) {
        expenseDao.softDelete(expenseId)
        try { supabaseClient.postgrest["expenses"].update({ set("is_deleted", true) }) { filter { eq("id", expenseId) } } } catch (_: Exception) {}
    }

    suspend fun clearDeletedExpenses(userId: String) {
        expenseDao.clearDeletedExpenses(userId)
        try { supabaseClient.postgrest["expenses"].delete { filter { eq("user_id", userId); eq("is_deleted", true) } } } catch (_: Exception) {}
    }

    suspend fun syncFromSupabase(userId: String) {
        try {
            val expenses = supabaseClient.postgrest["expenses"].select { filter { eq("user_id", userId) } }.decodeList<ExpenseDto>()
            expenseDao.insertAll(expenses.map { it.toEntity() })
        } catch (_: Exception) {}
    }

    suspend fun clearLocalData(userId: String) = expenseDao.deleteAllForUser(userId)
}

@Serializable data class ExpenseDto(
    val id: String, val user_id: String, val amount: Double, val category: String,
    val date: String, val description: String? = null, val is_deleted: Boolean = false,
    val created_at: String? = null, val updated_at: String? = null
) {
    fun toEntity() = ExpenseEntity(id, user_id, amount, category, date, description, is_deleted, created_at, updated_at)
    companion object { fun fromEntity(e: ExpenseEntity) = ExpenseDto(e.id, e.userId, e.amount, e.category, e.date, e.description, e.isDeleted, e.createdAt, e.updatedAt) }
}

// ═══════════════════════════════════════════════
// Calendar Event Repository
// ═══════════════════════════════════════════════

@Singleton
class CalendarEventRepository @Inject constructor(
    private val calendarEventDao: CalendarEventDao,
    private val supabaseClient: SupabaseClient
) {
    fun getCalendarEvents(userId: String): Flow<List<CalendarEventEntity>> = calendarEventDao.getCalendarEventsByUser(userId)
    fun getCalendarEventsByDate(userId: String, date: String): Flow<List<CalendarEventEntity>> =
        calendarEventDao.getCalendarEventsByDate(userId, date)

    suspend fun addCalendarEvent(userId: String, eventDate: String, name: String,
                                  time: String?, link: String?, comments: String?): CalendarEventEntity {
        val event = CalendarEventEntity(
            id = DateUtils.generateId(), userId = userId, eventDate = eventDate,
            name = name, time = time, link = link, comments = comments,
            createdAt = java.time.Instant.now().toString(), updatedAt = java.time.Instant.now().toString()
        )
        calendarEventDao.upsert(event)
        try { supabaseClient.postgrest["calendar_events"].upsert(CalendarEventDto.fromEntity(event)) } catch (_: Exception) {}
        return event
    }

    suspend fun deleteCalendarEvent(eventId: String) {
        calendarEventDao.deleteCalendarEvent(eventId)
        try { supabaseClient.postgrest["calendar_events"].delete { filter { eq("id", eventId) } } } catch (_: Exception) {}
    }

    suspend fun syncFromSupabase(userId: String) {
        try {
            val events = supabaseClient.postgrest["calendar_events"].select { filter { eq("user_id", userId) } }.decodeList<CalendarEventDto>()
            calendarEventDao.insertAll(events.map { it.toEntity() })
        } catch (_: Exception) {}
    }

    suspend fun clearLocalData(userId: String) = calendarEventDao.deleteAllForUser(userId)
}

@Serializable data class CalendarEventDto(
    val id: String, val user_id: String, val event_date: String, val name: String,
    val time: String? = null, val link: String? = null, val comments: String? = null,
    val created_at: String? = null, val updated_at: String? = null
) {
    fun toEntity() = CalendarEventEntity(id, user_id, event_date, name, time, link, comments, created_at, updated_at)
    companion object { fun fromEntity(e: CalendarEventEntity) = CalendarEventDto(e.id, e.userId, e.eventDate, e.name, e.time, e.link, e.comments, e.createdAt, e.updatedAt) }
}

// ═══════════════════════════════════════════════
// Education Fee Repository
// ═══════════════════════════════════════════════

@Singleton
class EducationFeeRepository @Inject constructor(
    private val educationFeeDao: EducationFeeDao,
    private val supabaseClient: SupabaseClient
) {
    fun getFees(userId: String): Flow<List<EducationFeeEntity>> = educationFeeDao.getFeesByUser(userId)

    suspend fun updateFee(userId: String, semester: Int, tuitionFee: Double, hostelFee: Double,
                           tuitionPaid: Boolean, hostelPaid: Boolean) {
        val fee = EducationFeeEntity(
            id = "fee_${userId}_$semester", userId = userId, semester = semester,
            tuitionFee = tuitionFee, hostelFee = hostelFee,
            tuitionPaid = tuitionPaid, hostelPaid = hostelPaid,
            updatedAt = java.time.Instant.now().toString()
        )
        educationFeeDao.upsert(fee)
        try { supabaseClient.postgrest["education_fees"].upsert(EducationFeeDto.fromEntity(fee)) } catch (_: Exception) {}
    }

    suspend fun syncFromSupabase(userId: String) {
        try {
            val fees = supabaseClient.postgrest["education_fees"].select { filter { eq("user_id", userId) } }.decodeList<EducationFeeDto>()
            educationFeeDao.insertAll(fees.map { it.toEntity() })
        } catch (_: Exception) {}
    }
}

@Serializable data class EducationFeeDto(
    val id: String, val user_id: String, val semester: Int,
    val tuition_fee: Double = 0.0, val hostel_fee: Double = 0.0,
    val tuition_paid: Boolean = false, val hostel_paid: Boolean = false,
    val updated_at: String? = null
) {
    fun toEntity() = EducationFeeEntity(id, user_id, semester, tuition_fee, hostel_fee, tuition_paid, hostel_paid, updated_at)
    companion object { fun fromEntity(e: EducationFeeEntity) = EducationFeeDto(e.id, e.userId, e.semester, e.tuitionFee, e.hostelFee, e.tuitionPaid, e.hostelPaid, e.updatedAt) }
}
