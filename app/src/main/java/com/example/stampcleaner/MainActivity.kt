package com.example.stampcleaner

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(17, 8, 24)
        window.navigationBarColor = android.graphics.Color.rgb(17, 8, 24)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFFF9DDF), onPrimary = Color(0xFF210D2B),
                secondary = Color(0xFFD28BFF), background = Color(0xFF0B0711),
                surface = Color(0xFF24132E), onSurface = Color(0xFFFFF0FA)
            )) {
                var bgUri by remember { mutableStateOf<Uri?>(null) }
                var bgVideo by remember { mutableStateOf(false) }
                var motion by remember { mutableStateOf(true) }
                val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) { bgUri = uri; bgVideo = true; motion = true }
                }
                val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) { bgUri = uri; bgVideo = false; motion = true }
                }
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF100817), Color(0xFF28102F), Color(0xFF100B20))))) {
                    if (bgUri != null && motion && bgVideo) {
                        key(bgUri) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { context -> VideoView(context).apply {
                                    setVideoURI(bgUri)
                                    setOnPreparedListener { player -> player.isLooping = true; player.setVolume(0f, 0f); start() }
                                } },
                                update = { view -> if (!view.isPlaying) view.start() }
                            )
                        }
                    } else if (bgUri != null && !bgVideo) {
                        AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                            android.widget.ImageView(context).apply {
                                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                                setImageURI(bgUri)
                            }
                        })
                    } else {
                        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                            drawCircle(Color(0x44F02DB7), size.minDimension * .48f, androidx.compose.ui.geometry.Offset(size.width * .12f, size.height * .18f))
                            drawCircle(Color(0x447B35E8), size.minDimension * .58f, androidx.compose.ui.geometry.Offset(size.width * .9f, size.height * .6f))
                        }
                    }
                    Box(Modifier.fillMaxSize().background(Color(0xB30B0711)))
                    StampCleanerScreen()
                    Surface(Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 8.dp), color = Color(0xDD24132E), shape = MaterialTheme.shapes.small) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { videoPicker.launch("video/*") }, contentPadding = PaddingValues(horizontal = 5.dp)) { Text("Видео", color = Color(0xFFFFB5E8), style = MaterialTheme.typography.labelSmall) }
                            TextButton(onClick = { imagePicker.launch("image/*") }, contentPadding = PaddingValues(horizontal = 5.dp)) { Text("Фото", color = Color(0xFFFFB5E8), style = MaterialTheme.typography.labelSmall) }
                            TextButton(onClick = { bgUri = null; motion = true }, contentPadding = PaddingValues(horizontal = 5.dp)) { Text("Сброс", color = Color(0xFFD8A0FF), style = MaterialTheme.typography.labelSmall) }
                            Switch(checked = motion, onCheckedChange = { motion = it }, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
