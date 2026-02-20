package com.vishnu.habittracker.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.data.local.entity.Subtask
import com.vishnu.habittracker.data.local.entity.TaskEntity
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

// ── Motivational Quotes (ported from quotes.js) ──────────────

private val quotes = listOf(
    "The secret of getting ahead is getting started." to "Mark Twain",
    "It does not matter how slowly you go as long as you do not stop." to "Confucius",
    "The only way to do great work is to love what you do." to "Steve Jobs",
    "Believe you can and you're halfway there." to "Theodore Roosevelt",
    "Success is not final, failure is not fatal: it is the courage to continue that counts." to "Winston Churchill",
    "The future belongs to those who believe in the beauty of their dreams." to "Eleanor Roosevelt",
    "Don't watch the clock; do what it does. Keep going." to "Sam Levenson",
    "Your time is limited, don't waste it living someone else's life." to "Steve Jobs",
    "The best time to plant a tree was 20 years ago. The second best time is now." to "Chinese Proverb",
    "Discipline is the bridge between goals and accomplishment." to "Jim Rohn"
)

/**
 * Dashboard Screen — the main screen matching the webapp's dashboard view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = HabitTrackerColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Quote Banner
            item { QuoteBanner() }

            // Priority Filter Bar
            item { PriorityFilterBar(currentFilter) { viewModel.setFilter(it) } }

            // Time Blocks
            item {
                TimeBlockCard(
                    title = "Morning",
                    emoji = "☀️",
                    timeRange = "6 AM – 12 PM",
                    accentColor = HabitTrackerColors.MorningAccent,
                    tasks = viewModel.getFilteredTasksByBlock(tasks, "morning"),
                    viewModel = viewModel
                )
            }
            item {
                TimeBlockCard(
                    title = "Evening",
                    emoji = "🌅",
                    timeRange = "12 PM – 6 PM",
                    accentColor = HabitTrackerColors.EveningAccent,
                    tasks = viewModel.getFilteredTasksByBlock(tasks, "evening"),
                    viewModel = viewModel
                )
            }
            item {
                TimeBlockCard(
                    title = "Night",
                    emoji = "🌙",
                    timeRange = "6 PM – 12 AM",
                    accentColor = HabitTrackerColors.NightAccent,
                    tasks = viewModel.getFilteredTasksByBlock(tasks, "night"),
                    viewModel = viewModel
                )
            }
        }
    }

    // Add/Edit Task Bottom Sheet
    if (formState.isSheetOpen) {
        AddTaskSheet(viewModel = viewModel)
    }
}

// ── Quote Banner ─────────────────────────────────────────────

@Composable
private fun QuoteBanner() {
    val (text, author) = remember { quotes.random() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            HabitTrackerColors.Primary.copy(alpha = 0.15f),
                            Color(0xFF8B5CF6).copy(alpha = 0.08f),
                            HabitTrackerColors.DarkBgSecondary.copy(alpha = 0.95f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "✨",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$text\"",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "— $author",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Priority Filter Bar ──────────────────────────────────────

@Composable
private fun PriorityFilterBar(currentFilter: String, onFilterChange: (String) -> Unit) {
    val filters = listOf(
        "all" to "All",
        "IAP" to "🔴 Urgent",
        "IBNU" to "🟡 Important",
        "NIBU" to "🔵 Delegate",
        "NINU" to "⚪ Defer"
    )
    val filterColors = mapOf(
        "IAP" to HabitTrackerColors.PriorityIAP,
        "IBNU" to HabitTrackerColors.PriorityIBNU,
        "NIBU" to HabitTrackerColors.PriorityNIBU,
        "NINU" to HabitTrackerColors.PriorityNINU
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (key, label) ->
            val selected = currentFilter == key
            val color = filterColors[key] ?: HabitTrackerColors.Primary

            FilterChip(
                selected = selected,
                onClick = { onFilterChange(key) },
                label = { Text(label, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
                    selectedLabelColor = color,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ── Time Block Card ──────────────────────────────────────────

@Composable
private fun TimeBlockCard(
    title: String,
    emoji: String,
    timeRange: String,
    accentColor: Color,
    tasks: List<TaskEntity>,
    viewModel: TaskViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon with colored glow background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = timeRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${tasks.size} tasks",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tasks
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks scheduled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskComplete(task.id) },
                            onEdit = { viewModel.openEditSheet(task) },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onToggleSubtask = { pathStr -> viewModel.toggleSubtaskByPath(task.id, pathStr) }
                        )
                    }
                }
            }
        }
    }
}

// ── Task Card ────────────────────────────────────────────────

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSubtask: (String) -> Unit
) {
    val priorityColors = mapOf(
        "IAP" to HabitTrackerColors.PriorityIAP,
        "IBNU" to HabitTrackerColors.PriorityIBNU,
        "NIBU" to HabitTrackerColors.PriorityNIBU,
        "NINU" to HabitTrackerColors.PriorityNINU
    )
    val priorityLabels = mapOf(
        "IAP" to "Important & Urgent",
        "IBNU" to "Important Not Urgent",
        "NIBU" to "Not Important Urgent",
        "NINU" to "Not Important Not Urgent"
    )
    val color = priorityColors[task.priority] ?: HabitTrackerColors.Primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (task.completed) 0.6f else 1f)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: checkbox + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = HabitTrackerColors.Success,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Meta: priority badge + time
            Row(
                modifier = Modifier.padding(start = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Priority badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = task.priority,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
                // Time
                if (task.hours > 0 || task.minutes > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.hours}h ${task.minutes}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Subtasks (recursive)
            if (task.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.padding(start = 48.dp)) {
                    task.subtasks.forEachIndexed { idx, subtask ->
                        SubtaskItem(
                            subtask = subtask,
                            pathStr = "$idx",
                            onToggle = onToggleSubtask
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit, colors = ButtonDefaults.textButtonColors(contentColor = HabitTrackerColors.Info)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = HabitTrackerColors.Danger)) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

// ── Recursive Subtask Item ───────────────────────────────────

@Composable
private fun SubtaskItem(subtask: Subtask, pathStr: String, onToggle: (String) -> Unit) {
    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(pathStr) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = subtask.completed,
                onCheckedChange = { onToggle(pathStr) },
                modifier = Modifier.size(20.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = HabitTrackerColors.Success,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = subtask.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (subtask.completed) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${subtask.duration}m",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Nested children
        if (subtask.children.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 28.dp)) {
                subtask.children.forEachIndexed { idx, child ->
                    SubtaskItem(
                        subtask = child,
                        pathStr = "$pathStr-$idx",
                        onToggle = onToggle
                    )
                }
            }
        }
    }
}

// ── Add/Edit Task Bottom Sheet ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskSheet(viewModel: TaskViewModel) {
    val formState by viewModel.formState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val blocks = listOf("morning" to "☀️ Morning", "evening" to "🌅 Evening", "night" to "🌙 Night")
    val priorities = listOf("IAP" to "🔴 Important & Urgent", "IBNU" to "🟡 Important Not Urgent",
                            "NIBU" to "🔵 Not Important Urgent", "NINU" to "⚪ Not Important Not Urgent")

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeSheet() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = if (formState.editingTaskId != null) "Edit Task" else "Add New Task",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            OutlinedTextField(
                value = formState.title,
                onValueChange = { viewModel.updateFormTitle(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Task Title") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Block selector
            Text("Time Block", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                blocks.forEach { (key, label) ->
                    FilterChip(
                        selected = formState.block == key,
                        onClick = { viewModel.updateFormBlock(key) },
                        label = { Text(label, fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HabitTrackerColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = HabitTrackerColors.Primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority selector
            Text("Priority", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                priorities.forEach { (key, label) ->
                    FilterChip(
                        selected = formState.priority == key,
                        onClick = { viewModel.updateFormPriority(key) },
                        label = { Text(label, fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HabitTrackerColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = HabitTrackerColors.Primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = formState.notes,
                onValueChange = { viewModel.updateFormNotes(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") },
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = { viewModel.saveTask() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HabitTrackerColors.Primary
                ),
                enabled = formState.title.isNotBlank()
            ) {
                Text(
                    text = if (formState.editingTaskId != null) "Update Task" else "Create Task",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
