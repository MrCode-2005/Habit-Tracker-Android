package com.vishnu.habittracker.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.HabitEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.HabitRepository
import com.vishnu.habittracker.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitFormState(
    val name: String = "",
    val editingHabitId: String? = null,
    val isSheetOpen: Boolean = false
)

/**
 * HabitViewModel — manages habits list and form state.
 * Port of: habits.js (155 lines)
 */
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val habits: StateFlow<List<HabitEntity>> = habitRepository.getHabits(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(HabitFormState())
    val formState: StateFlow<HabitFormState> = _formState.asStateFlow()

    fun openAddSheet() { _formState.value = HabitFormState(isSheetOpen = true) }
    fun openEditSheet(habit: HabitEntity) {
        _formState.value = HabitFormState(name = habit.name, editingHabitId = habit.id, isSheetOpen = true)
    }
    fun closeSheet() { _formState.value = _formState.value.copy(isSheetOpen = false) }
    fun updateName(name: String) { _formState.value = _formState.value.copy(name = name) }

    fun saveHabit() {
        val form = _formState.value
        if (form.name.isBlank()) return
        viewModelScope.launch {
            if (form.editingHabitId != null) {
                val existing = habits.value.find { it.id == form.editingHabitId } ?: return@launch
                habitRepository.updateHabit(existing.copy(name = form.name))
            } else {
                habitRepository.addHabit(userId, form.name)
            }
            _formState.value = HabitFormState()
        }
    }

    fun toggleToday(habitId: String) {
        viewModelScope.launch { habitRepository.toggleHabitToday(habitId) }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch { habitRepository.deleteHabit(habitId, userId) }
    }

    /** Calculate streak for a habit */
    fun getStreak(habit: HabitEntity): Pair<Int, Int> =
        habitRepository.calculateStreak(habit.completions)

    /** Check if habit is completed today */
    fun isCompletedToday(habit: HabitEntity): Boolean =
        habit.completions[DateUtils.todayKey()] == true
}
