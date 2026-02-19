package com.vishnu.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * Glassmorphism-style card matching the webapp's premium card design.
 *
 * Webapp CSS reference:
 *   background: linear-gradient(145deg, rgba(30,41,59,0.8), rgba(15,23,42,0.95));
 *   border-radius: 20px;
 *   box-shadow: 0 4px 20px rgba(0,0,0,0.2);
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, shape, ambientColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        HabitTrackerColors.GlassCardDark,
                        HabitTrackerColors.GlassCardDarkEnd
                    )
                )
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Light-mode card variant — simple surface card.
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        content = content
    )
}
