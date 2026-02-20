package com.vishnu.habittracker.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.Subtask
import com.vishnu.habittracker.data.local.entity.TaskEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusState(
    val isActive: Boolean = false,
    val isPaused: Boolean = true,
    val isBreakMode: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val taskTitle: String = "Focus Session",
    val subtaskTitle: String? = null,
    val subtaskIndex: Int = 0,         // current leaf subtask index
    val totalLeafSubtasks: Int = 0,
    val completedSubtasks: Int = 0,
    val breakDuration: Int = 5 * 60,   // 5 min default
    val savedWorkRemaining: Int = 0,
    val savedWorkTotal: Int = 0
)

/**
 * FocusViewModel — countdown timer, break mode, subtask navigation.
 * Port of core timer logic from focus-mode.js (5,460 lines).
 * Scoped to: timer, breaks, subtask nav, auto-complete.
 */
@HiltViewModel
class FocusViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.getTasks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var currentTask: TaskEntity? = null
    private var leafSubtasks: List<Subtask> = emptyList()

    // Focus quotes — port from focus-mode.js
    val focusQuotes = listOf(
        "The secret of getting ahead is getting started." to "Mark Twain",
        "Focus on being productive instead of busy." to "Tim Ferriss",
        "Concentrate all your thoughts upon the work at hand." to "Alexander Graham Bell",
        "The successful warrior is the average man, with laser-like focus." to "Bruce Lee",
        "Where focus goes, energy flows." to "Tony Robbins",
        "You can do anything, but not everything." to "David Allen",
        "The main thing is to keep the main thing the main thing." to "Stephen Covey",
        "Starve your distractions and feed your focus." to "Unknown",
        "Action is the foundational key to all success." to "Pablo Picasso",
        "Don't watch the clock; do what it does. Keep going." to "Sam Levenson"
    )

    /**
     * Open focus mode with a task — port of FocusMode.open()
     */
    fun openFocus(task: TaskEntity) {
        currentTask = task
        val flat = flattenSubtasks(task.subtasks)
        leafSubtasks = flat.map { it }

        val firstIncomplete = flat.indexOfFirst { !it.completed }
        val idx = if (firstIncomplete >= 0) firstIncomplete else 0

        val totalMinutes = if (flat.isNotEmpty()) {
            flat[idx].duration
        } else {
            task.hours * 60 + task.minutes
        }
        val totalSec = totalMinutes * 60

        _state.value = FocusState(
            isActive = true,
            isPaused = true,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            taskTitle = task.title,
            subtaskTitle = if (flat.isNotEmpty()) flat[idx].title else null,
            subtaskIndex = idx,
            totalLeafSubtasks = flat.size,
            completedSubtasks = flat.count { it.completed }
        )

        // Auto-start after a short delay
        viewModelScope.launch {
            delay(500)
            startTimer()
        }
    }

    fun toggleTimer() {
        if (_state.value.isPaused) startTimer() else pauseTimer()
    }

    fun startTimer() {
        if (!_state.value.isPaused) return
        _state.value = _state.value.copy(isPaused = false)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && !_state.value.isPaused) {
                delay(1000)
                if (!_state.value.isPaused) {
                    _state.value = _state.value.copy(
                        remainingSeconds = _state.value.remainingSeconds - 1
                    )
                }
            }
            if (_state.value.remainingSeconds <= 0) {
                onTimerComplete()
            }
        }
    }

    fun pauseTimer() {
        _state.value = _state.value.copy(isPaused = true)
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _state.value = _state.value.copy(
            remainingSeconds = _state.value.totalSeconds,
            isPaused = true
        )
    }

    /**
     * Break mode toggle — port of FocusMode.toggleBreakMode()
     */
    fun toggleBreakMode() {
        val s = _state.value
        if (s.isBreakMode) {
            // Return to work — restore saved time
            pauseTimer()
            _state.value = s.copy(
                isBreakMode = false,
                remainingSeconds = s.savedWorkRemaining,
                totalSeconds = s.savedWorkTotal
            )
        } else {
            // Enter break — save current work time
            pauseTimer()
            _state.value = s.copy(
                isBreakMode = true,
                savedWorkRemaining = s.remainingSeconds,
                savedWorkTotal = s.totalSeconds,
                remainingSeconds = s.breakDuration,
                totalSeconds = s.breakDuration
            )
        }
    }

    fun setBreakDuration(minutes: Int) {
        val secs = minutes * 60
        _state.value = _state.value.copy(breakDuration = secs)
        if (_state.value.isBreakMode) {
            pauseTimer()
            _state.value = _state.value.copy(
                remainingSeconds = secs,
                totalSeconds = secs
            )
        }
    }

    /**
     * Subtask navigation — port of FocusMode.navigateSubtask()
     */
    fun navigateSubtask(direction: Int) { // -1 = prev, +1 = next
        if (leafSubtasks.isEmpty()) return
        val s = _state.value
        val newIdx = (s.subtaskIndex + direction).coerceIn(0, leafSubtasks.size - 1)
        if (newIdx == s.subtaskIndex) return

        pauseTimer()
        val sub = leafSubtasks[newIdx]
        val totalSec = sub.duration * 60

        _state.value = s.copy(
            subtaskIndex = newIdx,
            subtaskTitle = sub.title,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            isPaused = true,
            completedSubtasks = leafSubtasks.count { it.completed }
        )
    }

    /**
     * Timer completion — auto-complete current subtask, advance to next.
     * Port of FocusMode.onTimerComplete()
     */
    private fun onTimerComplete() {
        val s = _state.value
        if (s.isBreakMode) {
            // Break over — return to work
            _state.value = s.copy(
                isBreakMode = false,
                remainingSeconds = s.savedWorkRemaining,
                totalSeconds = s.savedWorkTotal,
                isPaused = true
            )
            return
        }

        // Auto-advance to next incomplete subtask
        val nextIncomplete = leafSubtasks.withIndex()
            .filter { it.index > s.subtaskIndex && !it.value.completed }
            .firstOrNull()

        if (nextIncomplete != null) {
            val sub = nextIncomplete.value
            val totalSec = sub.duration * 60
            _state.value = s.copy(
                subtaskIndex = nextIncomplete.index,
                subtaskTitle = sub.title,
                totalSeconds = totalSec,
                remainingSeconds = totalSec,
                completedSubtasks = s.completedSubtasks + 1,
                isPaused = true
            )
        } else {
            // All done — mark the task as complete
            currentTask?.let { task ->
                viewModelScope.launch {
                    taskRepository.toggleTaskComplete(task.id, userId)
                }
            }
            _state.value = s.copy(
                isPaused = true,
                remainingSeconds = 0,
                completedSubtasks = leafSubtasks.size
            )
        }
    }

    fun closeFocus() {
        pauseTimer()
        _state.value = FocusState()
        currentTask = null
        leafSubtasks = emptyList()
    }

    // ── Helpers ────────────────────────────────────────────

    /** Flatten nested subtasks to leaf nodes only — port of flattenSubtasks() */
    private fun flattenSubtasks(subtasks: List<Subtask>): List<Subtask> {
        val result = mutableListOf<Subtask>()
        for (sub in subtasks) {
            if (sub.children.isEmpty()) {
                result.add(sub)
            } else {
                result.addAll(flattenSubtasks(sub.children))
            }
        }
        return result
    }

    /** Find path string for a subtask by title (e.g., "0-1-2") */
    private fun findSubtaskPath(subtasks: List<Subtask>, title: String, prefix: String = ""): String? {
        for ((i, sub) in subtasks.withIndex()) {
            val path = if (prefix.isEmpty()) "$i" else "$prefix-$i"
            if (sub.title == title) return path
            if (sub.children.isNotEmpty()) {
                val found = findSubtaskPath(sub.children, title, path)
                if (found != null) return found
            }
        }
        return null
    }
}
