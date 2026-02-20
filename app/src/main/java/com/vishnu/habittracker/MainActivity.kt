package com.vishnu.habittracker

import android.content.Intent
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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen — shows brand colors before Compose loads
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Edge-to-edge for immersive UI
        enableEdgeToEdge()

        // Handle deep link from OAuth callback (when app is cold-started)
        supabaseClient.handleDeeplinks(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link from OAuth callback (when app is already running)
        supabaseClient.handleDeeplinks(intent)
    }
}
