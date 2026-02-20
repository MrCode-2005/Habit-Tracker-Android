package com.vishnu.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.Subtask
import com.vishnu.habittracker.data.local.entity.TaskEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Task form state for the add/edit bottom sheet.
 */
data class TaskFormState(
    val title: String = "",
    val block: String = "morning",
    val priority: String = "IAP",
    val hours: Int = 0,
    val minutes: Int = 0,
    val notes: String = "",
    val subtasks: List<Subtask> = emptyList(),
    val editingTaskId: String? = null,
    val isSheetOpen: Boolean = false
)

/**
 * TaskViewModel — manages dashboard state.
 * Ports: tasks.js render, toggleComplete, toggleSubtaskByPath, saveTask, deleteTask
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    /** All tasks for the current user, observed reactively */
    val tasks: StateFlow<List<TaskEntity>> = taskRepository.getTasks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Current priority filter */
    private val _currentFilter = MutableStateFlow("all")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    /** Task form state */
    private val _formState = MutableStateFlow(TaskFormState())
    val formState: StateFlow<TaskFormState> = _formState.asStateFlow()

    // ── Filter ────────────────────────────────────

    fun setFilter(filter: String) {
        _currentFilter.value = filter
    }

    /** Get tasks filtered by priority and block */
    fun getFilteredTasksByBlock(tasks: List<TaskEntity>, block: String): List<TaskEntity> {
        val filtered = if (_currentFilter.value == "all") tasks
                       else tasks.filter { it.priority == _currentFilter.value }
        return filtered.filter { it.block == block }
    }

    // ── Task CRUD ─────────────────────────────────

    fun openAddSheet() {
        _formState.value = TaskFormState(isSheetOpen = true)
    }

    fun openEditSheet(task: TaskEntity) {
        _formState.value = TaskFormState(
            title = task.title,
            block = task.block,
            priority = task.priority,
            hours = task.hours,
            minutes = task.minutes,
            notes = task.notes ?: "",
            subtasks = task.subtasks,
            editingTaskId = task.id,
            isSheetOpen = true
        )
    }

    fun closeSheet() {
        _formState.value = _formState.value.copy(isSheetOpen = false)
    }

    fun updateFormTitle(title: String) { _formState.value = _formState.value.copy(title = title) }
    fun updateFormBlock(block: String) { _formState.value = _formState.value.copy(block = block) }
    fun updateFormPriority(priority: String) { _formState.value = _formState.value.copy(priority = priority) }
    fun updateFormNotes(notes: String) { _formState.value = _formState.value.copy(notes = notes) }

    /** Save task (add or edit) — mirrors tasks.js saveTask() */
    fun saveTask() {
        val form = _formState.value
        if (form.title.isBlank()) return

        viewModelScope.launch {
            if (form.editingTaskId != null) {
                val existing = tasks.value.find { it.id == form.editingTaskId } ?: return@launch
                taskRepository.updateTask(
                    existing.copy(
                        title = form.title, block = form.block, priority = form.priority,
                        hours = form.hours, minutes = form.minutes, notes = form.notes,
                        subtasks = form.subtasks
                    )
                )
            } else {
                taskRepository.addTask(
                    userId = userId, title = form.title, block = form.block,
                    priority = form.priority, hours = form.hours, minutes = form.minutes,
                    notes = form.notes, subtasks = form.subtasks
                )
            }
            _formState.value = TaskFormState() // Reset form
        }
    }

    /** Toggle task completion — mirrors tasks.js toggleComplete() */
    fun toggleTaskComplete(taskId: String) {
        viewModelScope.launch {
            taskRepository.toggleTaskComplete(taskId, userId)
        }
    }

    /** Delete task with confirmation reference */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId, userId)
        }
    }

    // ── Subtask Logic (exact port of tasks.js) ────

    /**
     * Toggle subtask by path string "0-1-2".
     * Port of: tasks.js toggleSubtaskByPath() (lines 441-483)
     */
    fun toggleSubtaskByPath(taskId: String, pathStr: String) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == taskId } ?: return@launch
            val subtasks = task.subtasks.toMutableDeepCopy()
            val path = pathStr.split("-").map { it.toInt() }

            val target = getSubtaskByPath(subtasks, path) ?: return@launch
            val newState = !target.completed

            // Set completion state
            setSubtaskCompleted(subtasks, path, newState)

            // If completing, also complete all children
            if (newState) {
                val targetRef = getSubtaskByPath(subtasks, path)!!
                setAllChildrenComplete(subtasks, path, true)
            }

            // If uncompleting, uncheck parent chain
            if (!newState) {
                uncheckParentChain(subtasks, path)
            }

            // If completing, check if siblings are all done → auto-complete parent
            if (newState) {
                updateParentCompletionStatus(subtasks, path)
            }

            // Check if all top-level subtasks are done → complete the task
            val allDone = areAllSubtasksComplete(subtasks)

            taskRepository.updateTask(
                task.copy(subtasks = subtasks, completed = allDone)
            )
        }
    }

    /** Navigate subtask tree by path indices — port of tasks.js getSubtaskByPath() */
    private fun getSubtaskByPath(subtasks: List<Subtask>, path: List<Int>): Subtask? {
        if (path.isEmpty()) return null
        var current = subtasks.getOrNull(path[0]) ?: return null
        for (i in 1 until path.size) {
            current = current.children.getOrNull(path[i]) ?: return null
        }
        return current
    }

    private fun setSubtaskCompleted(subtasks: MutableList<Subtask>, path: List<Int>, completed: Boolean) {
        if (path.size == 1) {
            val idx = path[0]
            subtasks[idx] = subtasks[idx].copy(completed = completed)
        } else {
            val idx = path[0]
            val childSubtasks = subtasks[idx].children.toMutableList()
            setSubtaskCompleted(childSubtasks as MutableList<Subtask>, path.drop(1), completed)
            subtasks[idx] = subtasks[idx].copy(children = childSubtasks)
        }
    }

    /** Port of tasks.js setAllChildrenComplete() (lines 486-493) */
    private fun setAllChildrenComplete(subtasks: MutableList<Subtask>, path: List<Int>, completed: Boolean) {
        val target = getSubtaskByPath(subtasks, path) ?: return
        if (target.children.isNotEmpty()) {
            for (i in target.children.indices) {
                val childPath = path + i
                setSubtaskCompleted(subtasks, childPath, completed)
                setAllChildrenComplete(subtasks, childPath, completed)
            }
        }
    }

    /** Port of tasks.js uncheckParentChain() (lines 496-514) */
    private fun uncheckParentChain(subtasks: MutableList<Subtask>, path: List<Int>) {
        for (depth in (path.size - 1) downTo 1) {
            val parentPath = path.take(depth)
            setSubtaskCompleted(subtasks, parentPath, false)
        }
    }

    /** Port of tasks.js updateParentCompletionStatus() (lines 518-551) */
    private fun updateParentCompletionStatus(subtasks: MutableList<Subtask>, path: List<Int>) {
        for (depth in path.size downTo 1) {
            val parentPath = path.take(depth - 1)
            val siblings = if (parentPath.isEmpty()) subtasks
                           else getSubtaskByPath(subtasks, parentPath)?.children ?: continue

            val allComplete = siblings.all { isSubtaskFullyComplete(it) }

            if (allComplete && parentPath.isNotEmpty()) {
                setSubtaskCompleted(subtasks, parentPath, true)
            }
        }
    }

    /** Port of tasks.js isSubtaskFullyComplete() (lines 555-561) */
    private fun isSubtaskFullyComplete(subtask: Subtask): Boolean {
        if (!subtask.completed) return false
        if (subtask.children.isNotEmpty()) {
            return subtask.children.all { isSubtaskFullyComplete(it) }
        }
        return true
    }

    /** Port of tasks.js areAllSubtasksComplete() (lines 564-567) */
    private fun areAllSubtasksComplete(subtasks: List<Subtask>): Boolean {
        if (subtasks.isEmpty()) return false
        return subtasks.all { isSubtaskFullyComplete(it) }
    }

    /** Load data from Supabase on first login */
    fun syncFromSupabase() {
        viewModelScope.launch {
            taskRepository.syncFromSupabase(userId)
        }
    }
}

/**
 * Deep copy a list of subtasks (since they're data classes with nested lists).
 * Needed for mutation before saving back to Room.
 */
fun List<Subtask>.toMutableDeepCopy(): MutableList<Subtask> {
    return this.map { subtask ->
        subtask.copy(
            children = subtask.children.toMutableDeepCopy()
        )
    }.toMutableList()
}
