package com.vishnu.habittracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Habit Tracker Design System — Colors
 *
 * All hex codes extracted directly from the webapp's main.css and dashboard.css.
 * Organized by semantic usage to ensure pixel-perfect parity.
 */
object HabitTrackerColors {

    // ── Brand ──────────────────────────────────────────────
    val Primary = Color(0xFF6366F1)         // Indigo — main brand accent
    val PrimaryHover = Color(0xFF4F46E5)    // Darker indigo for pressed states
    val PrimaryLight = Color(0xFFA5B4FC)    // Light indigo for focus rings, badges
    val PrimaryContainer = Color(0xFF312E81) // Deep indigo container

    // ── Status ─────────────────────────────────────────────
    val Success = Color(0xFF10B981)          // Emerald — completed, positive
    val SuccessHover = Color(0xFF059669)
    val Warning = Color(0xFFF59E0B)          // Amber — medium priority, breaks
    val WarningHover = Color(0xFFD97706)
    val Danger = Color(0xFFEF4444)           // Red — delete, errors, high priority
    val DangerHover = Color(0xFFDC2626)
    val Info = Color(0xFF3B82F6)             // Blue — information, stats
    val InfoHover = Color(0xFF2563EB)

    // ── Light Mode ─────────────────────────────────────────
    val LightBgPrimary = Color(0xFFF8F9FA)
    val LightBgSecondary = Color(0xFFFFFFFF)
    val LightBgTertiary = Color(0xFFE9ECEF)
    val LightBgHover = Color(0xFFF1F3F5)
    val LightTextPrimary = Color(0xFF212529)
    val LightTextSecondary = Color(0xFF495057)
    val LightTextTertiary = Color(0xFF6C757D)
    val LightBorder = Color(0xFFDEE2E6)

    // ── Dark Mode ──────────────────────────────────────────
    val DarkBgPrimary = Color(0xFF0F172A)    // Slate 900
    val DarkBgSecondary = Color(0xFF1E293B)  // Slate 800
    val DarkBgTertiary = Color(0xFF334155)   // Slate 700
    val DarkBgHover = Color(0xFF475569)      // Slate 600
    val DarkTextPrimary = Color(0xFFF1F5F9)  // Slate 100
    val DarkTextSecondary = Color(0xFFCBD5E1) // Slate 300
    val DarkTextTertiary = Color(0xFF94A3B8)  // Slate 400
    val DarkBorder = Color(0xFF334155)

    // ── Eisenhower Priority ────────────────────────────────
    val PriorityIAP = Color(0xFFEF4444)      // Important & Urgent
    val PriorityIBNU = Color(0xFFF59E0B)     // Important Not Urgent
    val PriorityNIBU = Color(0xFF3B82F6)     // Not Important But Urgent
    val PriorityNINU = Color(0xFF6B7280)     // Not Important Not Urgent

    val PriorityIAPLight = Color(0xFFF87171)
    val PriorityIBNULight = Color(0xFFFBBF24)
    val PriorityNIBULight = Color(0xFF60A5FA)
    val PriorityNINULight = Color(0xFF9CA3AF)

    // ── Time Blocks ────────────────────────────────────────
    val MorningAccent = Color(0xFFF59E0B)    // Amber
    val EveningAccent = Color(0xFFF97316)    // Orange
    val NightAccent = Color(0xFF6366F1)      // Indigo

    val MorningBg = Color(0x26F59E0B)        // 15% alpha
    val EveningBg = Color(0x26F97316)
    val NightBg = Color(0x266366F1)

    // ── Glassmorphism ──────────────────────────────────────
    val GlassCardDark = Color(0xCC1E293B)    // rgba(30, 41, 59, 0.8)
    val GlassCardDarkEnd = Color(0xF20F172A) // rgba(15, 23, 42, 0.95)
    val GlassOverlay = Color(0x99000000)     // rgba(0, 0, 0, 0.6) - modal backdrop

    // ── Heatmap Levels ─────────────────────────────────────
    val HeatmapLightEmpty = Color(0xFFEBEDF0)
    val HeatmapLightL1 = Color(0xFF9BE9A8)
    val HeatmapLightL2 = Color(0xFF40C463)
    val HeatmapLightL3 = Color(0xFF30A14E)
    val HeatmapLightL4 = Color(0xFF216E39)

    val HeatmapDarkEmpty = Color(0xFF161B22)
    val HeatmapDarkL1 = Color(0xFF0E4429)
    val HeatmapDarkL2 = Color(0xFF006D32)
    val HeatmapDarkL3 = Color(0xFF26A641)
    val HeatmapDarkL4 = Color(0xFF39D353)

    // ── Special Gradients (used as Brush objects) ──────────
    val GradientPrimaryStart = Color(0xFF6366F1)
    val GradientPrimaryEnd = Color(0xFF818CF8)
    val GradientStreakStart = Color(0xFFF97316)
    val GradientStreakEnd = Color(0xFFEF4444)
    val GradientQuoteStart = Color(0x266366F1)  // 15% alpha
    val GradientQuoteMid = Color(0x148B5CF6)    // 8% alpha
    val GradientQuoteEnd = Color(0xF21E293B)     // 95% alpha
    val GradientFocusBgStart = Color(0xFF1A1A2E)
    val GradientFocusBgEnd = Color(0xFF0F0F23)

    // ── Theme Presets ──────────────────────────────────────
    val PresetOceanPrimary = Color(0xFF0EA5E9)
    val PresetSunsetPrimary = Color(0xFFF59E0B)
    val PresetForestPrimary = Color(0xFF22C55E)
    val PresetMidnightPrimary = Color(0xFF8B5CF6)
    val PresetRosePrimary = Color(0xFFF43F5E)
    val PresetNeonPrimary = Color(0xFF22D3EE)
}
