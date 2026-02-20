package com.vishnu.habittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.vishnu.habittracker.data.local.entity.TaskEntity
import com.vishnu.habittracker.data.local.entity.TaskCompletionHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Task DAO — CRUD operations for tasks.
 * Mirrors state.js: getTasks, addTask, updateTask, deleteTask, toggleTaskComplete
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY created_at DESC")
    fun getTasksByUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND block = :block ORDER BY priority ASC")
    fun getTasksByBlock(userId: String, block: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM tasks WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    // ── Task Completion History ──────────────────
    @Query("SELECT * FROM task_completion_history WHERE user_id = :userId ORDER BY date_key DESC")
    fun getTaskHistory(userId: String): Flow<List<TaskCompletionHistoryEntity>>

    @Upsert
    suspend fun upsertTaskHistory(history: TaskCompletionHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTaskHistory(history: List<TaskCompletionHistoryEntity>)

    @Query("DELETE FROM task_completion_history WHERE user_id = :userId AND task_id = :taskId")
    suspend fun deleteTaskHistory(userId: String, taskId: String)
}
