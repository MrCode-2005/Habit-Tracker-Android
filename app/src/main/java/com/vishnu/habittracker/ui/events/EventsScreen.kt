package com.vishnu.habittracker.ui.events

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.data.local.entity.EventEntity
import com.vishnu.habittracker.ui.theme.HabitTrackerColors
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Events Screen — countdown timers to upcoming events.
 * Port of: events.js createEventCard + updateEventCountdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(viewModel: EventViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = HabitTrackerColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Event") }
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Event, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No events scheduled", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Add your first upcoming event!", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(event = event, viewModel = viewModel)
                }
            }
        }
    }

    if (formState.isSheetOpen) { AddEventSheet(viewModel) }
}

@Composable
private fun EventCard(event: EventEntity, viewModel: EventViewModel) {
    val formatted = try {
        val dt = parseDateTime(event.dateTime)
        dt?.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")) ?: event.dateTime
    } catch (e: Exception) { event.dateTime }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatted, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.openEditSheet(event) }) {
                        Icon(Icons.Outlined.Edit, "Edit", tint = HabitTrackerColors.Info)
                    }
                    IconButton(onClick = { viewModel.deleteEvent(event.id) }) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = HabitTrackerColors.Danger)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Live countdown
            EventCountdownTimer(dateTime = event.dateTime)
        }
    }
}

/**
 * Live event countdown — port of events.js updateEventCountdown()
 */
@Composable
private fun EventCountdownTimer(dateTime: String) {
    var timeText by remember { mutableStateOf("Calculating...") }
    var isExpired by remember { mutableStateOf(false) }

    LaunchedEffect(dateTime) {
        while (true) {
            val dt = parseDateTime(dateTime)
            if (dt != null) {
                val eventMillis = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val diff = eventMillis - System.currentTimeMillis()
                if (diff <= 0) {
                    timeText = "Event Started! 🎉"
                    isExpired = true
                } else {
                    val days = (diff / 86400000).toInt()
                    val hours = ((diff % 86400000) / 3600000).toInt()
                    val minutes = ((diff % 3600000) / 60000).toInt()
                    val seconds = ((diff % 60000) / 1000).toInt()
                    timeText = "${days}d ${hours}h ${minutes}m ${seconds}s"
                    isExpired = false
                }
            }
            delay(1000L)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isExpired) HabitTrackerColors.Success.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isExpired) HabitTrackerColors.Success else HabitTrackerColors.Primary
            )
            Text("Time Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun parseDateTime(dateTime: String): LocalDateTime? {
    return try {
        // Handle ISO instant format
        val instant = Instant.parse(dateTime)
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    } catch (e: Exception) {
        try {
            LocalDateTime.parse(dateTime.take(19))
        } catch (e2: Exception) {
            try { LocalDate.parse(dateTime.take(10)).atStartOfDay() } catch (e3: Exception) { null }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventSheet(viewModel: EventViewModel) {
    val formState by viewModel.formState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Local date/time state for picker
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeSheet() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                if (formState.editingEventId != null) "Edit Event" else "Add New Event",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = formState.name, onValueChange = { viewModel.updateName(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Event Name") },
                placeholder = { Text("e.g. Birthday Party") },
                shape = RoundedCornerShape(12.dp), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Date selection button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Time selection button
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val dt = selectedDate.atTime(selectedHour, selectedMinute)
                    val isoString = dt.atZone(ZoneId.systemDefault()).toInstant().toString()
                    viewModel.updateDateTime(isoString)
                    viewModel.saveEvent()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HabitTrackerColors.Primary),
                enabled = formState.name.isNotBlank()
            ) {
                Text(if (formState.editingEventId != null) "Update Event" else "Create Event", color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }
}
