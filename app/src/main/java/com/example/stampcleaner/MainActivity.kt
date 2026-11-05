package com.example.stampcleaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(22, 39, 34)
        window.navigationBarColor = android.graphics.Color.rgb(22, 39, 34)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFFFC66D),
                onPrimary = Color(0xFF2A2118),
                secondary = Color(0xFF79D6C2),
                background = Color(0xFF142720),
                surface = Color(0xFF20372F),
                onSurface = Color(0xFFF5F0E6)
            )) { StampCleanerScreen() }
        }
    }
}
