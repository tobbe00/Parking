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
import androidx.lifecycle.lifecycleScope
import com.example.parking.data.callVisionApi
import com.example.parking.ui.screens.*
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            testGoogleCredentials(applicationContext)
        }
        setContent {
            ParkingApp()
        }
    }
}

suspend fun testGoogleCredentials(context: Context) {
    withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.assets.open("service-account-key.json")
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/cloud-vision"))

            val token = credentials.refreshAccessToken().tokenValue
            Log.d("GoogleCredentialsTest", "Token genererat! Access Token: $token")
        } catch (e: Exception) {
            Log.e("GoogleCredentialsTest", "Fel vid läsning av nyckel eller token-generering", e)
        }
    }
}



@Composable
fun ParkingApp() {
    // Håller reda på vilken skärm som visas
    var currentScreen by remember { mutableStateOf("HomeScreen") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var visionResults by remember { mutableStateOf<List<String>>(emptyList()) } // Fixad variabel

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
                onRetake = {
                    currentScreen = "TakePicture"
                },
                onSend = { receivedResults ->
                    // Hantera resultaten från Vision API
                    visionResults = receivedResults
                    currentScreen = "ResultScreen"
                }
            )
        }
        "ResultScreen" -> {
            ResultScreen(
                results = visionResults,
                onBack = {
                    currentScreen = "HomeScreen"
                }
            )
        }
    }
}
