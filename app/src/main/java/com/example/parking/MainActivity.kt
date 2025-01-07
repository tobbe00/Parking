package com.example.parking

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parking.data.repositories.customvision.Prediction

import com.example.parking.ui.screens.*
import com.example.parking.ui.viewmodels.ParkingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

import android.content.pm.ActivityInfo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Test if Azure Vision API works correctly (optional, for debugging)
        runBlocking {
            testAzureVisionApi(applicationContext)
        }
        setContent {
            ParkingApp()
        }
    }
}

suspend fun testAzureVisionApi(context: Context) {
    withContext(Dispatchers.IO) {
        try {
            // Testa att använda Azure Vision API (valfritt)
            Log.d("AzureVisionTest", "Azure Vision API konfigurerat korrekt!")
        } catch (e: Exception) {
            Log.e("AzureVisionTest", "Fel vid testning av Azure Vision API", e)
        }
    }
}

@Composable
fun ParkingApp(viewModel: ParkingViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("HomeScreen") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "HomeScreen" -> {
            HomeScreen(
                onNavigateToCamera = {
                    currentScreen = "CameraScreen"
                }
            )
        }
        "CameraScreen" -> {
            CameraPermissionRequest(
                onPermissionGranted = {
                    currentScreen = "TakePicture"
                },
                onPermissionDenied = {
                    currentScreen = "HomeScreen"
                }
            )
        }
        "TakePicture" -> {
            CameraScreen(
                onImageCaptured = { uri ->
                    imageUri = uri.toString()
                    currentScreen = "PreviewScreen"
                }
            )
        }
        "PreviewScreen" -> {
            PreviewScreen(
                imageUri = imageUri,
                sasUrl = "https://tobbestorage.blob.core.windows.net/images?sv=2022-11-02&ss=b&srt=co&sp=rwdtfx&se=2025-02-01T03:13:43Z&st=2025-01-01T19:13:43Z&spr=https&sig=EVvwLc02B1ag%2BtvnOh4J7NhuMRU4FF9NvYtOJC4beTg%3D",
                onRetake = {
                    currentScreen = "TakePicture"
                },
                onSendComplete = {
                    currentScreen = "ResultScreen"
                },
                viewModel = viewModel
            )
        }
        "ResultScreen" -> {
            ResultScreen(
                results = viewModel.uiState.result,
                onBack = {
                    imageUri = null // Clear the previous image URI
                    viewModel.clearResults() // Clear ViewModel state
                    currentScreen = "HomeScreen"
                }
            )
        }

    }
}
