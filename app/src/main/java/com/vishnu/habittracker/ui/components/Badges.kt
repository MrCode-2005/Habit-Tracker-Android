package com.vishnu.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * Priority badge matching the webapp's Eisenhower matrix badges.
 *
 * CSS reference: gradient background + white text + small border-radius
 */
@Composable
fun PriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (colors, label) = when (priority.uppercase()) {
        "IAP" -> Pair(
            listOf(HabitTrackerColors.PriorityIAP, HabitTrackerColors.PriorityIAPLight),
            "Important & Urgent"
        )
        "IBNU" -> Pair(
            listOf(HabitTrackerColors.PriorityIBNU, HabitTrackerColors.PriorityIBNULight),
            "Important"
        )
        "NIBU" -> Pair(
            listOf(HabitTrackerColors.PriorityNIBU, HabitTrackerColors.PriorityNIBULight),
            "Urgent"
        )
        else -> Pair(
            listOf(HabitTrackerColors.PriorityNINU, HabitTrackerColors.PriorityNINULight),
            "Low"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                brush = Brush.linearGradient(colors = colors)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

/**
 * Time block header badge (Morning/Evening/Night)
 */
@Composable
fun TimeBlockBadge(
    block: String,
    modifier: Modifier = Modifier
) {
    val (color, emoji, label) = when (block.lowercase()) {
        "morning" -> Triple(HabitTrackerColors.MorningAccent, "☀️", "Morning")
        "evening" -> Triple(HabitTrackerColors.EveningAccent, "🌅", "Evening")
        "night" -> Triple(HabitTrackerColors.NightAccent, "🌙", "Night")
        else -> Triple(HabitTrackerColors.Primary, "📋", block)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
