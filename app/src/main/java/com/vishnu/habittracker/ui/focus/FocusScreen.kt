package com.vishnu.habittracker.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * FocusScreen — immersive focus timer experience.
 * Port of: focus-mode.js (core timer UI, progress ring, break mode, subtask nav)
 */
@Composable
fun FocusScreen(
    taskId: String,
    viewModel: FocusViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    // Open focus mode when task is found
    LaunchedEffect(tasks, taskId) {
        if (!state.isActive && tasks.isNotEmpty()) {
            val task = tasks.find { it.id == taskId }
            if (task != null) {
                viewModel.openFocus(task)
            }
        }
    }

    val quote = remember { viewModel.focusQuotes.random() }

    // Gradient background — port of focus-mode.css (#1a1a2e → #0f0f23)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        HabitTrackerColors.GradientFocusBgStart,
                        HabitTrackerColors.GradientFocusBgEnd
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.closeFocus()
                    onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    if (state.isBreakMode) "☕ Break Time" else "🎯 Focus Mode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task info
            Text(
                state.taskTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            if (state.subtaskTitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Sub-task: ${state.subtaskTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HabitTrackerColors.PrimaryLight,
                    textAlign = TextAlign.Center
                )
            }

            // Subtask progress bar
            if (state.totalLeafSubtasks > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = if (state.totalLeafSubtasks > 0)
                    state.completedSubtasks.toFloat() / state.totalLeafSubtasks else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(0.7f).height(6.dp),
                    color = HabitTrackerColors.Success,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "${state.completedSubtasks}/${state.totalLeafSubtasks} completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Timer ring + display
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                val fraction = if (state.totalSeconds > 0)
                    state.remainingSeconds.toFloat() / state.totalSeconds else 1f
                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(300), label = "timer"
                )
                val ringColor = if (state.isBreakMode) HabitTrackerColors.Warning else HabitTrackerColors.Primary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    val pad = stroke.width / 2
                    val arcSize = Size(size.width - pad * 2, size.height - pad * 2)

                    // Background ring
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = Offset(pad, pad), size = arcSize, style = stroke
                    )
                    // Progress arc
                    drawArc(
                        color = ringColor,
                        startAngle = -90f, sweepAngle = 360f * animatedFraction, useCenter = false,
                        topLeft = Offset(pad, pad), size = arcSize, style = stroke
                    )
                }

                // Digital clock
                val mins = state.remainingSeconds / 60
                val secs = state.remainingSeconds % 60
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold, fontSize = 56.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset button
                FilledTonalIconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(Icons.Default.Refresh, "Reset", tint = Color.White, modifier = Modifier.size(24.dp))
                }

                // Play/Pause — large button
                FloatingActionButton(
                    onClick = { viewModel.toggleTimer() },
                    modifier = Modifier.size(72.dp),
                    containerColor = if (state.isBreakMode) HabitTrackerColors.Warning else HabitTrackerColors.Primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (state.isPaused) "Start" else "Pause",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Break toggle
                FilledTonalIconButton(
                    onClick = { viewModel.toggleBreakMode() },
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (state.isBreakMode) HabitTrackerColors.Warning.copy(alpha = 0.3f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        Icons.Default.Coffee, "Break",
                        tint = if (state.isBreakMode) HabitTrackerColors.Warning else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Break duration options (visible when in break mode)
            if (state.isBreakMode) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 20, 30).forEach { min ->
                        val selected = state.breakDuration == min * 60
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setBreakDuration(min) },
                            label = { Text("${min}m", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HabitTrackerColors.Warning.copy(alpha = 0.3f),
                                selectedLabelColor = HabitTrackerColors.Warning,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Subtask navigation
            if (state.totalLeafSubtasks > 1) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateSubtask(-1) },
                        enabled = state.subtaskIndex > 0
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        "${state.subtaskIndex + 1} of ${state.totalLeafSubtasks}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    IconButton(
                        onClick = { viewModel.navigateSubtask(1) },
                        enabled = state.subtaskIndex < state.totalLeafSubtasks - 1
                    ) {
                        Icon(Icons.Default.ChevronRight, "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Focus quote
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✨", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "\"${quote.first}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        "— ${quote.second}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
