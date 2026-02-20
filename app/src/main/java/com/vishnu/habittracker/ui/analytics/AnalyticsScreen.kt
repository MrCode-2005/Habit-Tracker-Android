package com.vishnu.habittracker.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * Analytics Screen — productivity insights dashboard.
 * Port of: analytics.js renderProductivityStats + chart sections
 */
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val goals by viewModel.goals.collectAsState()

    val stats = viewModel.computeStats(tasks, habits, goals)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats overview grid
        item {
            Text("Productivity Insights", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.TaskAlt, label = "Tasks Done", value = "${stats.completedTasks}", color = HabitTrackerColors.Primary)
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.LocalFireDepartment, label = "Day Streak", value = "${stats.taskStreak}", color = HabitTrackerColors.Warning)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.CalendarToday, label = "Best Day", value = stats.mostProductiveDay, color = HabitTrackerColors.Info)
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.CheckCircle,
                    label = "Habits Today", value = "${stats.habitsCompletedToday}/${stats.totalHabits}", color = HabitTrackerColors.Success)
            }
        }

        // Goals summary
        item {
            Text("Goals Overview", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalStatBadge(modifier = Modifier.weight(1f), label = "Completed", value = stats.goalsCompleted, color = HabitTrackerColors.Success)
                GoalStatBadge(modifier = Modifier.weight(1f), label = "Active", value = stats.goalsActive, color = HabitTrackerColors.Warning)
                GoalStatBadge(modifier = Modifier.weight(1f), label = "Failed", value = stats.goalsFailed, color = HabitTrackerColors.Danger)
            }
        }

        // Weekly task completion bar chart
        item {
            Text("Weekly Task Completion", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        item { BarChartCard(data = stats.weeklyTaskData, color = HabitTrackerColors.Primary, label = "Tasks") }

        // Weekly habit completion bar chart
        item {
            Text("Weekly Habit Completion", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        item { BarChartCard(data = stats.weeklyHabitData, color = HabitTrackerColors.Success, label = "Habits") }
    }
}

@Composable
private fun StatCard(modifier: Modifier, icon: ImageVector, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GoalStatBadge(modifier: Modifier, label: String, value: Int, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

/**
 * Simple composable bar chart — replaces Chart.js bar charts from analytics.js
 */
@Composable
private fun BarChartCard(data: List<Pair<String, Int>>, color: Color, label: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (dayLabel, percent) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Value label
                        Text("${percent}%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        val barHeight = (percent / 100f * 80).coerceAtLeast(4f)
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(listOf(color, color.copy(alpha = 0.6f)))
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Day label
                        Text(dayLabel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
