package com.vishnu.habittracker.ui.goals

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.data.local.entity.GoalEntity
import com.vishnu.habittracker.ui.theme.HabitTrackerColors
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Goals Screen — countdown timers with type filters.
 * Port of: goals.js createGoalElement + startCountdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val filteredGoals = viewModel.getFilteredGoals(goals)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = HabitTrackerColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Goal") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter tabs
            item { GoalFilterTabs(currentFilter) { viewModel.setFilter(it) } }

            if (filteredGoals.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.EmojiEvents, null, modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No goals yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Create your first goal to start tracking!", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            } else {
                items(filteredGoals, key = { it.id }) { goal ->
                    GoalCard(goal = goal, viewModel = viewModel)
                }
            }
        }
    }

    if (formState.isSheetOpen) { AddGoalSheet(viewModel = viewModel) }
}

@Composable
private fun GoalFilterTabs(currentFilter: String, onFilter: (String) -> Unit) {
    val filters = listOf("all" to "All", "weekly" to "Weekly", "monthly" to "Monthly", "custom" to "Custom")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (key, label) ->
            FilterChip(
                selected = currentFilter == key,
                onClick = { onFilter(key) },
                label = { Text(label, fontSize = 13.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HabitTrackerColors.Primary.copy(alpha = 0.2f),
                    selectedLabelColor = HabitTrackerColors.Primary
                )
            )
        }
    }
}

@Composable
private fun GoalCard(goal: GoalEntity, viewModel: GoalViewModel) {
    val typeLabels = mapOf("weekly" to "Weekly Goal", "monthly" to "Monthly Goal", "custom" to "${goal.duration}-Day Goal")
    val typeColors = mapOf("weekly" to HabitTrackerColors.Info, "monthly" to HabitTrackerColors.Warning, "custom" to HabitTrackerColors.Primary)
    val color = typeColors[goal.type] ?: HabitTrackerColors.Primary

    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (goal.completed) 0.7f else 1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                        Text(typeLabels[goal.type] ?: goal.type, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.toggleComplete(goal.id) }) {
                        Icon(
                            if (goal.completed) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Toggle complete",
                            tint = if (goal.completed) HabitTrackerColors.Success else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = HabitTrackerColors.Danger)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DateChip(icon = Icons.Outlined.CalendarToday, label = "Start", date = goal.startDate)
                DateChip(icon = Icons.Outlined.EventAvailable, label = "End", date = goal.endDate)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown timer
            if (!goal.completed) {
                CountdownTimer(endDate = goal.endDate)
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = HabitTrackerColors.Success.copy(alpha = 0.1f)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = HabitTrackerColors.Success, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Goal Completed! 🎉", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = HabitTrackerColors.Success)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, date: String) {
    val formatted = try {
        LocalDate.parse(date.take(10)).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) { date.take(10) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(4.dp))
        Text("$label: $formatted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Live countdown timer — updates every second.
 * Port of: goals.js startCountdown() (lines 236-289)
 */
@Composable
private fun CountdownTimer(endDate: String) {
    var timeLeft by remember { mutableStateOf(calculateTimeLeft(endDate)) }

    LaunchedEffect(endDate) {
        while (true) {
            timeLeft = calculateTimeLeft(endDate)
            delay(1000L)
        }
    }

    if (timeLeft == null) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            color = HabitTrackerColors.Danger.copy(alpha = 0.1f)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.TimerOff, null, tint = HabitTrackerColors.Danger, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Time's Up!", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = HabitTrackerColors.Danger)
            }
        }
    } else {
        val (days, hours, minutes, seconds) = timeLeft!!
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CountdownUnit(value = days, label = "Days")
            CountdownUnit(value = hours, label = "Hours")
            CountdownUnit(value = minutes, label = "Min")
            CountdownUnit(value = seconds, label = "Sec")
        }
    }
}

@Composable
private fun CountdownUnit(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                text = value.toString().padStart(2, '0'),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = HabitTrackerColors.Primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class TimeLeft(val days: Int, val hours: Int, val minutes: Int, val seconds: Int)

private fun calculateTimeLeft(endDateStr: String): TimeLeft? {
    return try {
        val endDate = LocalDate.parse(endDateStr.take(10))
        val endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val now = java.time.Instant.now()
        val diff = endInstant.toEpochMilli() - now.toEpochMilli()
        if (diff <= 0) return null

        val totalSeconds = diff / 1000
        val days = (totalSeconds / 86400).toInt()
        val hours = ((totalSeconds % 86400) / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        TimeLeft(days, hours, minutes, seconds)
    } catch (e: Exception) { null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalSheet(viewModel: GoalViewModel) {
    val formState by viewModel.formState.collectAsState()
    val types = listOf("weekly" to "📅 Weekly (7 days)", "monthly" to "📆 Monthly (30 days)", "custom" to "⏳ Custom")

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeSheet() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                if (formState.editingGoalId != null) "Edit Goal" else "Add New Goal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = formState.title, onValueChange = { viewModel.updateTitle(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Goal Title") },
                placeholder = { Text("e.g. Complete Android course") },
                shape = RoundedCornerShape(12.dp), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Goal Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                types.forEach { (key, label) ->
                    FilterChip(
                        selected = formState.type == key, onClick = { viewModel.updateType(key) },
                        label = { Text(label, fontSize = 13.sp) }, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HabitTrackerColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = HabitTrackerColors.Primary
                        )
                    )
                }
            }

            // Custom days input
            AnimatedVisibility(visible = formState.type == "custom") {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = formState.customDays.toString(),
                        onValueChange = { viewModel.updateCustomDays(it.toIntOrNull() ?: 7) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Number of Days") },
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveGoal() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HabitTrackerColors.Primary),
                enabled = formState.title.isNotBlank()
            ) {
                Text(if (formState.editingGoalId != null) "Update Goal" else "Create Goal", color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
