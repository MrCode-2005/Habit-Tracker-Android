package com.vishnu.habittracker.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.GoalEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class GoalFormState(
    val title: String = "",
    val type: String = "weekly",     // weekly, monthly, custom
    val customDays: Int = 7,
    val editingGoalId: String? = null,
    val isSheetOpen: Boolean = false
)

/**
 * GoalViewModel — manages goals with countdown logic.
 * Port of: goals.js (321 lines) — saveGoal with duration calc, filter, toggleComplete
 */
@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val goals: StateFlow<List<GoalEntity>> = goalRepository.getGoals(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentFilter = MutableStateFlow("all")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    private val _formState = MutableStateFlow(GoalFormState())
    val formState: StateFlow<GoalFormState> = _formState.asStateFlow()

    fun setFilter(filter: String) { _currentFilter.value = filter }

    fun getFilteredGoals(goals: List<GoalEntity>): List<GoalEntity> {
        return if (_currentFilter.value == "all") goals
               else goals.filter { it.type == _currentFilter.value }
    }

    fun openAddSheet() { _formState.value = GoalFormState(isSheetOpen = true) }
    fun openEditSheet(goal: GoalEntity) {
        _formState.value = GoalFormState(
            title = goal.title, type = goal.type,
            customDays = goal.duration, editingGoalId = goal.id, isSheetOpen = true
        )
    }
    fun closeSheet() { _formState.value = _formState.value.copy(isSheetOpen = false) }
    fun updateTitle(title: String) { _formState.value = _formState.value.copy(title = title) }
    fun updateType(type: String) { _formState.value = _formState.value.copy(type = type) }
    fun updateCustomDays(days: Int) { _formState.value = _formState.value.copy(customDays = days) }

    /**
     * Save goal — mirrors goals.js saveGoal() (lines 99-146)
     * Calculates endDate from type: weekly (+7d), monthly (+30d), custom (+N)
     */
    fun saveGoal() {
        val form = _formState.value
        if (form.title.isBlank()) return

        viewModelScope.launch {
            val now = LocalDate.now()
            val endDate = when (form.type) {
                "weekly" -> now.plusDays(7)
                "monthly" -> now.plusMonths(1)
                else -> now.plusDays(form.customDays.toLong())
            }
            val duration = when (form.type) {
                "weekly" -> 7
                "monthly" -> 30
                else -> form.customDays
            }

            if (form.editingGoalId != null) {
                val existing = goals.value.find { it.id == form.editingGoalId } ?: return@launch
                goalRepository.updateGoal(existing.copy(
                    title = form.title, type = form.type, duration = duration,
                    startDate = now.toString(), endDate = endDate.toString()
                ))
            } else {
                goalRepository.addGoal(
                    userId = userId, title = form.title, type = form.type,
                    duration = duration, startDate = now.toString(), endDate = endDate.toString()
                )
            }
            _formState.value = GoalFormState()
        }
    }

    fun toggleComplete(goalId: String) {
        viewModelScope.launch { goalRepository.toggleGoalComplete(goalId) }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch { goalRepository.deleteGoal(goalId, userId) }
    }
}
