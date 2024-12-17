package com.example.parking.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.parking.data.callVisionApi
import kotlinx.coroutines.launch

@Composable
fun PreviewScreen(
    imageUri: String?,
    onRetake: () -> Unit,
    onSend: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Visa bilden
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Captured Image",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentScale = ContentScale.Crop
        )

        // Knapparna för att ta om bilden eller skicka den till Vision API
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onRetake) {
                Text("Ta om")
            }
            Button(onClick = {
                coroutineScope.launch {
                    // Skicka bilden till Vision API
                    val results = callVisionApi(context, Uri.parse(imageUri))
                    onSend(results)
                }
            }) {
                Text("Skicka")
            }
        }
    }
}
