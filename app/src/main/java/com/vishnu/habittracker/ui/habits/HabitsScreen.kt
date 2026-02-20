package com.vishnu.habittracker.ui.habits

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.data.local.entity.HabitEntity
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * Habits Screen — daily habit tracking with streak display.
 * Port of: habits.js createHabitCard()
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: HabitViewModel = hiltViewModel()) {
    val habits by viewModel.habits.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = HabitTrackerColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Habit") }
        }
    ) { padding ->
        if (habits.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.TrackChanges, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No habits yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Create your first daily habit!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitCard(habit = habit, viewModel = viewModel)
                }
            }
        }
    }

    if (formState.isSheetOpen) {
        AddHabitSheet(viewModel = viewModel)
    }
}

@Composable
private fun HabitCard(habit: HabitEntity, viewModel: HabitViewModel) {
    val isCompletedToday = viewModel.isCompletedToday(habit)
    val (currentStreak, longestStreak) = viewModel.getStreak(habit)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: name + checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = isCompletedToday,
                    onCheckedChange = { viewModel.toggleToday(habit.id) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = HabitTrackerColors.Success,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Streak stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakStat(value = currentStreak, label = "Current", color = HabitTrackerColors.Primary)
                StreakStat(value = longestStreak, label = "Best", color = HabitTrackerColors.Warning)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { viewModel.openEditSheet(habit) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = HabitTrackerColors.Info)
                }
                IconButton(
                    onClick = { viewModel.deleteHabit(habit.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = HabitTrackerColors.Danger)
                }
            }
        }
    }
}

@Composable
private fun StreakStat(value: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHabitSheet(viewModel: HabitViewModel) {
    val formState by viewModel.formState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeSheet() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                text = if (formState.editingHabitId != null) "Edit Habit" else "Add New Habit",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Habit Name") },
                placeholder = { Text("e.g. Read for 30 minutes") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveHabit() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HabitTrackerColors.Primary),
                enabled = formState.name.isNotBlank()
            ) {
                Text(if (formState.editingHabitId != null) "Update Habit" else "Create Habit", color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
