package com.vishnu.habittracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape system — mirrors the webapp's border-radius values.
 */
val HabitTrackerShapes = Shapes(
    // Small: chips, badges (--radius-sm: 6px)
    small = RoundedCornerShape(6.dp),
    // Medium: buttons, inputs (--radius-md: 8px → --radius-lg: 12px)
    medium = RoundedCornerShape(12.dp),
    // Large: cards, modals (premium 20px radius)
    large = RoundedCornerShape(20.dp),
    // Extra large: bottom sheets, full-card containers
    extraLarge = RoundedCornerShape(28.dp)
)
