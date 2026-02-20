package com.vishnu.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vishnu.habittracker.ui.theme.HabitTrackerColors
import kotlinx.coroutines.delay

/**
 * Toast type with styling — port of toast.js notification system.
 */
enum class ToastType(val icon: @Composable () -> Unit, val color: Color) {
    SUCCESS(
        { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
        HabitTrackerColors.Success
    ),
    ERROR(
        { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
        HabitTrackerColors.Danger
    ),
    WARNING(
        { Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
        HabitTrackerColors.Warning
    ),
    INFO(
        { Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
        HabitTrackerColors.Info
    )
}

/**
 * Toast composable — shows a brief message at the bottom of the screen.
 * Port of: toast.js showToast() functionality.
 */
@Composable
fun Toast(
    message: String,
    type: ToastType = ToastType.SUCCESS,
    durationMs: Long = 3000,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        delay(durationMs)
        onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(type.color)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            type.icon()
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "Dismiss", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
