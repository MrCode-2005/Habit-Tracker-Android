package com.vishnu.habittracker.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.data.local.entity.CalendarEventEntity
import com.vishnu.habittracker.ui.theme.HabitTrackerColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calendar Screen — monthly grid view with day click for events.
 * Port of: calendar.js render() + onDayClick() + renderUpcomingEvents()
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val formState by viewModel.formState.collectAsState()

    val datesWithEvents = viewModel.getDatesWithEvents(calendarEvents)
    val selectedDateEvents = selectedDate?.let {
        viewModel.getEventsForDate(it.toString(), calendarEvents)
    } ?: emptyList()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet(selectedDate) },
                containerColor = HabitTrackerColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Calendar Event") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month navigation header
            item { MonthHeader(currentMonth, viewModel) }

            // Calendar grid
            item {
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    datesWithEvents = datesWithEvents,
                    onDateClick = { viewModel.selectDate(it) }
                )
            }

            // Today button
            item {
                TextButton(onClick = { viewModel.goToToday() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Today, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Go to Today")
                }
            }

            // Selected date events
            if (selectedDate != null) {
                item {
                    Text(
                        text = "Events on ${selectedDate!!.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (selectedDateEvents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("No events on this date", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(selectedDateEvents, key = { it.id }) { event ->
                        CalendarEventCard(event, viewModel)
                    }
                }
            }
        }
    }

    if (formState.isSheetOpen) { AddCalendarEventSheet(viewModel) }
}

@Composable
private fun MonthHeader(currentMonth: YearMonth, viewModel: CalendarViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { viewModel.navigateMonth(-1) }) {
            Icon(Icons.Default.ChevronLeft, "Previous Month")
        }
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { viewModel.navigateMonth(1) }) {
            Icon(Icons.Default.ChevronRight, "Next Month")
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    datesWithEvents: Set<String>,
    onDateClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstDay = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val startDayOfWeek = firstDay.dayOfWeek.value % 7 // Sunday = 0

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Day headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day cells
            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = week * 7 + col
                        if (cellIndex < startDayOfWeek || dayCounter > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = currentMonth.atDay(dayCounter)
                            val isToday = date == today
                            val isSelected = date == selectedDate
                            val hasEvents = datesWithEvents.contains(date.toString())

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> HabitTrackerColors.Primary
                                            isToday -> HabitTrackerColors.Primary.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateClick(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayCounter",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> HabitTrackerColors.Primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    // Event dot indicator
                                    if (hasEvents) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) Color.White
                                                    else HabitTrackerColors.Primary
                                                )
                                        )
                                    }
                                }
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarEventCard(event: CalendarEventEntity, viewModel: CalendarViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    if (!event.time.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (!event.link.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Link, null, modifier = Modifier.size(14.dp), tint = HabitTrackerColors.Info)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.link, style = MaterialTheme.typography.bodySmall, color = HabitTrackerColors.Info, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (!event.comments.isNullOrBlank()) {
                        Text(event.comments, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.openEditSheet(event) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Edit, "Edit", modifier = Modifier.size(16.dp), tint = HabitTrackerColors.Info)
                    }
                    IconButton(onClick = { viewModel.deleteCalendarEvent(event.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, "Delete", modifier = Modifier.size(16.dp), tint = HabitTrackerColors.Danger)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCalendarEventSheet(viewModel: CalendarViewModel) {
    val formState by viewModel.formState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeSheet() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("Add Calendar Event", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("Date: ${formState.selectedDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(value = formState.name, onValueChange = { viewModel.updateFormName(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Event Name") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(value = formState.time, onValueChange = { viewModel.updateFormTime(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Time (optional)") }, placeholder = { Text("e.g. 2:00 PM") },
                shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(value = formState.link, onValueChange = { viewModel.updateFormLink(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Link (optional)") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(value = formState.comments, onValueChange = { viewModel.updateFormComments(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Comments (optional)") }, shape = RoundedCornerShape(12.dp), minLines = 2, maxLines = 3)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveCalendarEvent() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HabitTrackerColors.Primary),
                enabled = formState.name.isNotBlank()
            ) { Text("Save Event", color = Color.White) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
