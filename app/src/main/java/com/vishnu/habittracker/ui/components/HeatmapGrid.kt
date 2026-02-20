package com.vishnu.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishnu.habittracker.ui.theme.HabitTrackerColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * GitHub-style heatmap composable.
 * Port of: heatmap.js (541 lines) — the grid layout, color levels, month labels, day labels.
 *
 * @param completions Map of "YYYY-MM-DD" → Boolean
 * @param months Number of months to display (default 6)
 * @param cellSize Size of each day cell
 * @param totalHabits For aggregate heatmap: total habits to scale level (1 for individual)
 */
@Composable
fun HeatmapGrid(
    completions: Map<String, Boolean>,
    months: Int = 6,
    cellSize: Dp = 14.dp,
    totalHabits: Int = 1,
    onDayClick: ((LocalDate) -> Unit)? = null
) {
    val today = LocalDate.now()
    val startDate = today.minusMonths(months.toLong()).with(DayOfWeek.MONDAY)

    // Build week columns (each column = 7 days, Mon-Sun)
    val totalDays = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
    val weeks = mutableListOf<List<Pair<LocalDate?, Int>>>() // date → level
    var dayIndex = 0
    // Align start to Monday
    val firstDow = startDate.dayOfWeek.value // 1=Mon ... 7=Sun

    // Build first partial week if needed
    while (dayIndex < totalDays) {
        val weekDays = mutableListOf<Pair<LocalDate?, Int>>()
        for (dow in 1..7) { // Mon=1 .. Sun=7
            val date = startDate.plusDays(dayIndex.toLong())
            if (dayIndex < totalDays && date.dayOfWeek.value == dow) {
                val dateKey = date.toString()
                val count = if (totalHabits > 1) {
                    // Aggregate mode: count how many habits completed this day
                    completions.entries.count { (k, v) -> k == dateKey && v }
                } else {
                    if (completions[dateKey] == true) 1 else 0
                }
                val level = calculateLevel(count, totalHabits)
                weekDays.add(date to level)
                dayIndex++
            } else if (dayIndex >= totalDays) {
                weekDays.add(null to 0) // Empty cell
            } else {
                weekDays.add(null to 0)
            }
        }
        weeks.add(weekDays)
    }

    // Find month boundaries for labels
    val monthLabels = mutableListOf<Pair<Int, String>>() // weekIndex → monthName
    var lastMonth = -1
    for ((weekIdx, week) in weeks.withIndex()) {
        val firstDay = week.firstOrNull { it.first != null }?.first
        if (firstDay != null && firstDay.monthValue != lastMonth) {
            lastMonth = firstDay.monthValue
            monthLabels.add(weekIdx to firstDay.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
        }
    }

    val isDark = true // We use dark mode heatmap colors

    Column {
        // Month labels row
        Row(modifier = Modifier.padding(start = 28.dp)) {
            LazyRow {
                items(weeks.indices.toList()) { weekIdx ->
                    val label = monthLabels.find { it.first == weekIdx }
                    Box(modifier = Modifier.width(cellSize + 2.dp)) {
                        if (label != null) {
                            Text(
                                label.second,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.offset(x = (-2).dp)
                            )
                        }
                    }
                }
            }
        }

        Row {
            // Day labels (Mon, Wed, Fri)
            Column(
                modifier = Modifier.padding(end = 4.dp, top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("", "Mon", "", "Wed", "", "Fri", "").forEach { label ->
                    Box(modifier = Modifier.height(cellSize), contentAlignment = Alignment.CenterStart) {
                        Text(
                            label, fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Grid
            LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                items(weeks) { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        week.forEach { (date, level) ->
                            val color = getHeatmapColor(level, isDark)
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                                    .then(
                                        if (date != null && date == today) {
                                            Modifier.border(1.dp, HabitTrackerColors.Primary, RoundedCornerShape(3.dp))
                                        } else Modifier
                                    )
                                    .then(
                                        if (date != null && onDayClick != null) {
                                            Modifier.clickable { onDayClick(date) }
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.padding(start = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Less", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            for (level in 0..4) {
                Box(
                    modifier = Modifier
                        .size(cellSize - 2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(getHeatmapColor(level, isDark))
                )
            }
            Text("More", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Map completion count → level 0-4 */
private fun calculateLevel(count: Int, totalHabits: Int): Int {
    if (count == 0) return 0
    if (totalHabits <= 1) return if (count > 0) 4 else 0
    val ratio = count.toFloat() / totalHabits
    return when {
        ratio >= 1.0f -> 4
        ratio >= 0.75f -> 3
        ratio >= 0.5f -> 2
        ratio > 0f -> 1
        else -> 0
    }
}

/** Get heatmap cell color by level — port of heatmap.css color vars */
private fun getHeatmapColor(level: Int, isDark: Boolean): Color {
    return if (isDark) {
        when (level) {
            0 -> HabitTrackerColors.HeatmapDarkEmpty
            1 -> HabitTrackerColors.HeatmapDarkL1
            2 -> HabitTrackerColors.HeatmapDarkL2
            3 -> HabitTrackerColors.HeatmapDarkL3
            4 -> HabitTrackerColors.HeatmapDarkL4
            else -> HabitTrackerColors.HeatmapDarkEmpty
        }
    } else {
        when (level) {
            0 -> HabitTrackerColors.HeatmapLightEmpty
            1 -> HabitTrackerColors.HeatmapLightL1
            2 -> HabitTrackerColors.HeatmapLightL2
            3 -> HabitTrackerColors.HeatmapLightL3
            4 -> HabitTrackerColors.HeatmapLightL4
            else -> HabitTrackerColors.HeatmapLightEmpty
        }
    }
}
