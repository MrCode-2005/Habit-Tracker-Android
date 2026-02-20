package com.vishnu.habittracker.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.EventEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventFormState(
    val name: String = "",
    val dateTime: String = "",   // ISO string
    val editingEventId: String? = null,
    val isSheetOpen: Boolean = false
)

/**
 * EventViewModel — manages event countdowns.
 * Port of: events.js (199 lines)
 */
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val events: StateFlow<List<EventEntity>> = eventRepository.getEvents(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(EventFormState())
    val formState: StateFlow<EventFormState> = _formState.asStateFlow()

    fun openAddSheet() { _formState.value = EventFormState(isSheetOpen = true) }
    fun openEditSheet(event: EventEntity) {
        _formState.value = EventFormState(name = event.name, dateTime = event.dateTime, editingEventId = event.id, isSheetOpen = true)
    }
    fun closeSheet() { _formState.value = _formState.value.copy(isSheetOpen = false) }
    fun updateName(name: String) { _formState.value = _formState.value.copy(name = name) }
    fun updateDateTime(dateTime: String) { _formState.value = _formState.value.copy(dateTime = dateTime) }

    fun saveEvent() {
        val form = _formState.value
        if (form.name.isBlank() || form.dateTime.isBlank()) return
        viewModelScope.launch {
            if (form.editingEventId != null) {
                val existing = events.value.find { it.id == form.editingEventId } ?: return@launch
                eventRepository.updateEvent(existing.copy(name = form.name, dateTime = form.dateTime))
            } else {
                eventRepository.addEvent(userId, form.name, form.dateTime)
            }
            _formState.value = EventFormState()
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch { eventRepository.deleteEvent(eventId) }
    }
}
