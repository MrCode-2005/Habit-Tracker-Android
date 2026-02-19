package com.vishnu.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.vishnu.habittracker.ui.HabitTrackerApp
import com.vishnu.habittracker.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen — shows brand colors before Compose loads
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Edge-to-edge for immersive UI
        enableEdgeToEdge()

        setContent {
            HabitTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HabitTrackerApp()
                }
            }
        }
    }
}
