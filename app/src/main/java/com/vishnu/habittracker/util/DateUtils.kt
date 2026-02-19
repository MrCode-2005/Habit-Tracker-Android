package com.vishnu.habittracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Date and formatting utilities — mirrors helper functions from the webapp's state.js
 */
object DateUtils {

    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /** Get today's date key in 'YYYY-MM-DD' format (matching webapp's dateKey format) */
    fun todayKey(): String = dateKeyFormat.format(Date())

    /** Get yesterday's date key */
    fun yesterdayKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateKeyFormat.format(cal.time)
    }

    /** Convert a Date to a date key string */
    fun toDateKey(date: Date): String = dateKeyFormat.format(date)

    /** Parse a date key string to a Date */
    fun fromDateKey(key: String): Date? = try {
        dateKeyFormat.parse(key)
    } catch (e: Exception) {
        null
    }

    /** Format seconds to HH:MM:SS (matching webapp's timer display) */
    fun formatTimer(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    /** Format amount as Indian Rupees (matching webapp's formatAmount) */
    fun formatRupees(amount: Double): String {
        return "₹${String.format(Locale.US, "%,.2f", amount)}"
    }

    /** Generate a unique ID (matching webapp's crypto.randomUUID or Date.now fallback) */
    fun generateId(): String = UUID.randomUUID().toString()
}
