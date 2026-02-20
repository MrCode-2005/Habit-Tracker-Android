package com.vishnu.habittracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * Theme preset data — port of customize.js (541 lines) theme presets.
 */
data class ThemePreset(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val success: Color,
    val background: Color,
    val emoji: String
)

val themePresets = listOf(
    ThemePreset("Default", Color(0xFF7C3AED), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFF0F0F23), "🎨"),
    ThemePreset("Ocean", Color(0xFF0EA5E9), Color(0xFF06B6D4), Color(0xFF14B8A6), Color(0xFF0C1222), "🌊"),
    ThemePreset("Sunset", Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFF97316), Color(0xFF1A0F0A), "🌅"),
    ThemePreset("Forest", Color(0xFF22C55E), Color(0xFF15803D), Color(0xFF4ADE80), Color(0xFF0A1A0F), "🌲"),
    ThemePreset("Midnight", Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFFA78BFA), Color(0xFF0F0F1A), "🌙"),
    ThemePreset("Rose", Color(0xFFF43F5E), Color(0xFFEC4899), Color(0xFFFB7185), Color(0xFF1A0A0F), "🌹"),
    ThemePreset("Neon", Color(0xFF22D3EE), Color(0xFFA855F7), Color(0xFF34D399), Color(0xFF0A0A15), "⚡")
)

/**
 * Settings Screen — Theme customization with 7 preset themes.
 * Port of: customize.js (541 lines) — theme presets, color pickers, font picker.
 *
 * Note: Dynamic theme switching is a preview — full DataStore persistence
 * and runtime theme application would require CompositionLocal overrides.
 * For now this shows the preset grid and selected state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedTheme by remember { mutableStateOf("Default") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Theme Presets section
            item {
                Text(
                    "🎨 Theme Presets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Choose a preset theme to customize the app's look and feel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Preset grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    themePresets.forEach { preset ->
                        ThemePresetCard(
                            preset = preset,
                            isSelected = selectedTheme == preset.name,
                            onClick = { selectedTheme = preset.name }
                        )
                    }
                }
            }

            // Color Preview section
            item {
                val current = themePresets.find { it.name == selectedTheme } ?: themePresets[0]
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🔮 Color Preview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ColorSwatch("Primary", current.primary)
                            ColorSwatch("Secondary", current.secondary)
                            ColorSwatch("Success", current.success)
                            ColorSwatch("Background", current.background)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Preview button
                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = current.primary),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) { Text("Preview Button", color = Color.White) }
                    }
                }
            }

            // App info
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨ Habit Tracker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("v1.0.0 • Built with ❤️ by Vishnu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ThemePresetCard(preset: ThemePreset, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(2.dp, preset.primary, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview dots
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(preset.primary, preset.secondary))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(preset.emoji, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(preset.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    listOf(preset.primary, preset.secondary, preset.success, preset.background).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                }
            }

            if (isSelected) {
                Text("Active", style = MaterialTheme.typography.labelSmall, color = preset.primary)
            }
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
