package com.vishnu.habittracker.ui.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.ui.components.HeatmapGrid
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * Individual habit progress screen with GitHub-style heatmap.
 * Port of: habit-progress.js (140 lines) — heatmap for single habit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitProgressScreen(
    habitId: String,
    viewModel: HabitViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val habit = habits.find { it.id == habitId }

    val timeRanges = listOf(12 to "Year", 6 to "6 Months", 3 to "3 Months", 1 to "Month")
    var selectedRange by remember { mutableIntStateOf(6) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.name ?: "Habit Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (habit == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HabitTrackerColors.Primary)
            }
            return@Scaffold
        }

        val completions = habit.completions
        val completedDays = completions.count { it.value }
        val streaks = viewModel.getStreak(habit)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("🔥", "Current Streak", "${streaks.first} days", HabitTrackerColors.Warning, Modifier.weight(1f))
                StatCard("🏆", "Best Streak", "${streaks.second} days", HabitTrackerColors.Success, Modifier.weight(1f))
                StatCard("📅", "Total Days", "$completedDays", HabitTrackerColors.Info, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time range filter
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                timeRanges.forEach { (months, label) ->
                    FilterChip(
                        selected = selectedRange == months,
                        onClick = { selectedRange = months },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HabitTrackerColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = HabitTrackerColors.Primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Heatmap
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Completion History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HeatmapGrid(
                        completions = completions,
                        months = selectedRange,
                        cellSize = 14.dp,
                        totalHabits = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    emoji: String, label: String, value: String, color: Color, modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
