package com.vishnu.habittracker.data.repository

import com.vishnu.habittracker.data.local.dao.TaskDao
import com.vishnu.habittracker.data.local.entity.Subtask
import com.vishnu.habittracker.data.local.entity.TaskCompletionHistoryEntity
import com.vishnu.habittracker.data.local.entity.TaskEntity
import com.vishnu.habittracker.util.DateUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Task Repository — local-first with Supabase sync.
 *
 * Pattern: Write to Room → return immediately → sync to Supabase in background
 * Mirrors: state.js addTask/updateTask/deleteTask + supabase.js upsertTask/deleteTask
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val supabaseClient: SupabaseClient,
    private val json: Json
) {
    /** Observe all tasks for a user (reactive Flow from Room) */
    fun getTasks(userId: String): Flow<List<TaskEntity>> = taskDao.getTasksByUser(userId)

    /** Observe tasks by time block */
    fun getTasksByBlock(userId: String, block: String): Flow<List<TaskEntity>> =
        taskDao.getTasksByBlock(userId, block)

    /** Get task completion history */
    fun getTaskHistory(userId: String): Flow<List<TaskCompletionHistoryEntity>> =
        taskDao.getTaskHistory(userId)

    /** Add a new task (local + remote) */
    suspend fun addTask(userId: String, title: String, block: String, priority: String,
                        hours: Int = 0, minutes: Int = 0, notes: String? = null,
                        subtasks: List<Subtask> = emptyList()): TaskEntity {
        val task = TaskEntity(
            id = DateUtils.generateId(),
            userId = userId,
            title = title,
            block = block,
            priority = priority,
            hours = hours,
            minutes = minutes,
            notes = notes,
            subtasks = subtasks,
            completed = false,
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        taskDao.upsert(task)
        syncTaskToSupabase(userId, task)
        return task
    }

    /** Update an existing task */
    suspend fun updateTask(task: TaskEntity) {
        val updated = task.copy(updatedAt = java.time.Instant.now().toString())
        taskDao.upsert(updated)
        syncTaskToSupabase(updated.userId, updated)
    }

    /** Toggle task completion (mirrors state.js toggleTaskComplete) */
    suspend fun toggleTaskComplete(taskId: String, userId: String): TaskEntity? {
        val task = taskDao.getTaskById(taskId) ?: return null
        val now = java.time.Instant.now().toString()
        val updated = task.copy(
            completed = !task.completed,
            completedAt = if (!task.completed) now else null,
            updatedAt = now
        )
        taskDao.upsert(updated)
        syncTaskToSupabase(userId, updated)
        return updated
    }

    /** Delete a task (local + remote). Records history if completed. */
    suspend fun deleteTask(taskId: String, userId: String) {
        val task = taskDao.getTaskById(taskId)
        // Record completion history before deleting (mirrors state.js deleteTask)
        if (task != null && task.completed && task.completedAt != null) {
            val history = TaskCompletionHistoryEntity(
                userId = userId,
                taskId = task.id,
                title = task.title,
                block = task.block,
                priority = task.priority,
                completedAt = task.completedAt,
                dateKey = task.completedAt.split("T")[0]
            )
            taskDao.upsertTaskHistory(history)
        }
        taskDao.deleteTask(taskId)
        deleteTaskFromSupabase(taskId)
    }

    /** Initial data load from Supabase → Room (called on first login) */
    suspend fun syncFromSupabase(userId: String) {
        try {
            val result = supabaseClient.postgrest["tasks"]
                .select { filter { eq("user_id", userId) } }
            val tasks = result.decodeList<TaskSupabaseDto>()
            val entities = tasks.map { it.toEntity() }
            taskDao.insertAll(entities)
        } catch (e: Exception) {
            // Silently fail — app works offline with existing Room data
        }
    }

    /** Clear all local data for a user (used on logout/account switch) */
    suspend fun clearLocalData(userId: String) {
        taskDao.deleteAllForUser(userId)
    }

    // ── Private Supabase sync helpers ─────────────

    private suspend fun syncTaskToSupabase(userId: String, task: TaskEntity) {
        try {
            supabaseClient.postgrest["tasks"].upsert(
                TaskSupabaseDto.fromEntity(task)
            )
        } catch (e: Exception) {
            // Queue for later sync via WorkManager
        }
    }

    private suspend fun deleteTaskFromSupabase(taskId: String) {
        try {
            supabaseClient.postgrest["tasks"]
                .delete { filter { eq("id", taskId) } }
        } catch (e: Exception) {
            // Queue for later
        }
    }
}

/**
 * DTO for Supabase serialization (snake_case field names match the DB columns).
 */
@Serializable
data class TaskSupabaseDto(
    val id: String,
    val user_id: String,
    val title: String,
    val block: String,
    val priority: String,
    val hours: Int = 0,
    val minutes: Int = 0,
    val notes: String? = null,
    val subtasks: List<Subtask> = emptyList(),
    val completed: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    fun toEntity() = TaskEntity(
        id = id, userId = user_id, title = title, block = block,
        priority = priority, hours = hours, minutes = minutes,
        notes = notes, subtasks = subtasks, completed = completed,
        createdAt = created_at, updatedAt = updated_at
    )

    companion object {
        fun fromEntity(e: TaskEntity) = TaskSupabaseDto(
            id = e.id, user_id = e.userId, title = e.title, block = e.block,
            priority = e.priority, hours = e.hours, minutes = e.minutes,
            notes = e.notes, subtasks = e.subtasks, completed = e.completed,
            created_at = e.createdAt, updated_at = e.updatedAt
        )
    }
}
