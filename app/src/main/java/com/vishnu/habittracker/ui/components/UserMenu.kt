package com.vishnu.habittracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * User menu dropdown — port of user-menu.js (546 lines).
 * Shows email, settings link, and logout button.
 */
@Composable
fun UserMenu(
    email: String?,
    onSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "User menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp)
        ) {
            // User email
            if (!email.isNullOrBlank()) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("Signed in as", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(email, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = { },
                    leadingIcon = { Icon(Icons.Filled.AccountCircle, null, tint = HabitTrackerColors.Primary) },
                    enabled = false
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            }

            // Settings
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = { expanded = false; onSettings() },
                leadingIcon = { Icon(Icons.Outlined.Settings, null) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

            // Sign Out
            DropdownMenuItem(
                text = { Text("Sign Out", color = HabitTrackerColors.Danger) },
                onClick = { expanded = false; onSignOut() },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = HabitTrackerColors.Danger) }
            )
        }
    }
}
