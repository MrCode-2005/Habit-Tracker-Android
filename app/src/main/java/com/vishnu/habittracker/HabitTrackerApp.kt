package com.vishnu.habittracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Habit Tracker.
 * @HiltAndroidApp triggers Hilt's code generation for dependency injection.
 */
@HiltAndroidApp
class HabitTrackerApp : Application()
