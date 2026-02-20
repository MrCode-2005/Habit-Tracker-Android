package com.vishnu.habittracker.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.CalendarEventEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.CalendarEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarFormState(
    val name: String = "",
    val time: String = "",
    val link: String = "",
    val comments: String = "",
    val selectedDate: String = LocalDate.now().toString(),
    val editingEventId: String? = null,
    val isSheetOpen: Boolean = false
)

/**
 * CalendarViewModel — monthly calendar with rich event entries.
 * Port of: calendar.js (746 lines) — navigateMonth, onDayClick, saveEvent
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarEventRepository: CalendarEventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val calendarEvents: StateFlow<List<CalendarEventEntity>> = calendarEventRepository.getCalendarEvents(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _formState = MutableStateFlow(CalendarFormState())
    val formState: StateFlow<CalendarFormState> = _formState.asStateFlow()

    // Navigation
    fun navigateMonth(direction: Int) {
        _currentMonth.value = _currentMonth.value.plusMonths(direction.toLong())
    }

    fun goToToday() {
        _currentMonth.value = YearMonth.now()
        _selectedDate.value = LocalDate.now()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /** Get events for a specific date */
    fun getEventsForDate(date: String, allEvents: List<CalendarEventEntity>): List<CalendarEventEntity> {
        return allEvents.filter { it.eventDate == date }
    }

    /** Get dates that have events (for dot indicators) */
    fun getDatesWithEvents(allEvents: List<CalendarEventEntity>): Set<String> {
        return allEvents.map { it.eventDate }.toSet()
    }

    // Form
    fun openAddSheet(date: LocalDate? = null) {
        _formState.value = CalendarFormState(
            selectedDate = (date ?: LocalDate.now()).toString(),
            isSheetOpen = true
        )
    }

    fun openEditSheet(event: CalendarEventEntity) {
        _formState.value = CalendarFormState(
            name = event.name, time = event.time ?: "",
            link = event.link ?: "", comments = event.comments ?: "",
            selectedDate = event.eventDate, editingEventId = event.id, isSheetOpen = true
        )
    }

    fun closeSheet() { _formState.value = _formState.value.copy(isSheetOpen = false) }
    fun updateFormName(name: String) { _formState.value = _formState.value.copy(name = name) }
    fun updateFormTime(time: String) { _formState.value = _formState.value.copy(time = time) }
    fun updateFormLink(link: String) { _formState.value = _formState.value.copy(link = link) }
    fun updateFormComments(comments: String) { _formState.value = _formState.value.copy(comments = comments) }

    fun saveCalendarEvent() {
        val form = _formState.value
        if (form.name.isBlank()) return
        viewModelScope.launch {
            if (form.editingEventId != null) {
                val existing = calendarEvents.value.find { it.id == form.editingEventId } ?: return@launch
                calendarEventRepository.addCalendarEvent(
                    userId = userId, eventDate = form.selectedDate, name = form.name,
                    time = form.time.ifBlank { null }, link = form.link.ifBlank { null },
                    comments = form.comments.ifBlank { null }
                )
            } else {
                calendarEventRepository.addCalendarEvent(
                    userId = userId, eventDate = form.selectedDate, name = form.name,
                    time = form.time.ifBlank { null }, link = form.link.ifBlank { null },
                    comments = form.comments.ifBlank { null }
                )
            }
            _formState.value = CalendarFormState()
        }
    }

    fun deleteCalendarEvent(eventId: String) {
        viewModelScope.launch { calendarEventRepository.deleteCalendarEvent(eventId) }
    }
}
