package com.vishnu.habittracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light color scheme — matches the webapp's light mode CSS variables.
 */
private val LightColorScheme = lightColorScheme(
    primary = HabitTrackerColors.Primary,
    onPrimary = HabitTrackerColors.LightBgSecondary,
    primaryContainer = HabitTrackerColors.PrimaryLight,
    onPrimaryContainer = HabitTrackerColors.PrimaryContainer,

    secondary = HabitTrackerColors.Info,
    onSecondary = HabitTrackerColors.LightBgSecondary,
    secondaryContainer = HabitTrackerColors.Info.copy(alpha = 0.15f),
    onSecondaryContainer = HabitTrackerColors.Info,

    tertiary = HabitTrackerColors.Success,
    onTertiary = HabitTrackerColors.LightBgSecondary,
    tertiaryContainer = HabitTrackerColors.Success.copy(alpha = 0.15f),
    onTertiaryContainer = HabitTrackerColors.Success,

    error = HabitTrackerColors.Danger,
    onError = HabitTrackerColors.LightBgSecondary,
    errorContainer = HabitTrackerColors.Danger.copy(alpha = 0.15f),
    onErrorContainer = HabitTrackerColors.Danger,

    background = HabitTrackerColors.LightBgPrimary,
    onBackground = HabitTrackerColors.LightTextPrimary,

    surface = HabitTrackerColors.LightBgSecondary,
    onSurface = HabitTrackerColors.LightTextPrimary,
    surfaceVariant = HabitTrackerColors.LightBgTertiary,
    onSurfaceVariant = HabitTrackerColors.LightTextSecondary,

    outline = HabitTrackerColors.LightBorder,
    outlineVariant = HabitTrackerColors.LightBorder.copy(alpha = 0.5f),

    inverseSurface = HabitTrackerColors.DarkBgSecondary,
    inverseOnSurface = HabitTrackerColors.DarkTextPrimary,
    inversePrimary = HabitTrackerColors.PrimaryLight,

    surfaceTint = HabitTrackerColors.Primary,
    scrim = HabitTrackerColors.GlassOverlay
)

/**
 * Dark color scheme — matches the webapp's dark mode CSS variables.
 */
private val DarkColorScheme = darkColorScheme(
    primary = HabitTrackerColors.Primary,
    onPrimary = HabitTrackerColors.DarkTextPrimary,
    primaryContainer = HabitTrackerColors.PrimaryContainer,
    onPrimaryContainer = HabitTrackerColors.PrimaryLight,

    secondary = HabitTrackerColors.Info,
    onSecondary = HabitTrackerColors.DarkTextPrimary,
    secondaryContainer = HabitTrackerColors.Info.copy(alpha = 0.15f),
    onSecondaryContainer = HabitTrackerColors.Info,

    tertiary = HabitTrackerColors.Success,
    onTertiary = HabitTrackerColors.DarkTextPrimary,
    tertiaryContainer = HabitTrackerColors.Success.copy(alpha = 0.15f),
    onTertiaryContainer = HabitTrackerColors.Success,

    error = HabitTrackerColors.Danger,
    onError = HabitTrackerColors.DarkTextPrimary,
    errorContainer = HabitTrackerColors.Danger.copy(alpha = 0.15f),
    onErrorContainer = HabitTrackerColors.Danger,

    background = HabitTrackerColors.DarkBgPrimary,
    onBackground = HabitTrackerColors.DarkTextPrimary,

    surface = HabitTrackerColors.DarkBgSecondary,
    onSurface = HabitTrackerColors.DarkTextPrimary,
    surfaceVariant = HabitTrackerColors.DarkBgTertiary,
    onSurfaceVariant = HabitTrackerColors.DarkTextSecondary,

    outline = HabitTrackerColors.DarkBorder,
    outlineVariant = HabitTrackerColors.DarkBorder.copy(alpha = 0.5f),

    inverseSurface = HabitTrackerColors.LightBgSecondary,
    inverseOnSurface = HabitTrackerColors.LightTextPrimary,
    inversePrimary = HabitTrackerColors.Primary,

    surfaceTint = HabitTrackerColors.Primary,
    scrim = HabitTrackerColors.GlassOverlay
)

/**
 * Main theme composable for the Habit Tracker app.
 * Applies the correct color scheme, typography, and shapes.
 */
@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Apply status bar styling
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitTrackerTypography,
        shapes = HabitTrackerShapes,
        content = content
    )
}
