package com.example.parking.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

import com.example.parking.data.azureocr.AzureReadRepository
import com.example.parking.data.customvision.ParkingVisionRepository
import com.example.parking.data.uploadImageWithSasToken
import com.example.parking.ui.viewmodels.ParkingViewModel

import kotlinx.coroutines.launch

@Composable
fun PreviewScreen(
    imageUri: String?,
    sasUrl: String,
    onRetake: () -> Unit,
    onSendComplete: (Map<String, Any>) -> Unit,
    viewModel: ParkingViewModel = remember { ParkingViewModel() }
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    Log.d("PreviewScreen", "Renderar PreviewScreen")
    Log.d("PreviewScreen", "Image URI: $imageUri")
    Log.d("PreviewScreen", "UI State: $uiState")

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
            Button(onClick = {
                Log.d("PreviewScreen", "Ta om-knappen tryckt")
                onRetake()
            }) {
                Text("Ta om")
            }

            Button(onClick = {
                Log.d("PreviewScreen", "Skicka-knappen tryckt")
                viewModel.sendImage(context, Uri.parse(imageUri), sasUrl)
            }) {
                Text("Skicka")
            }
        }

        // Visa eventuella felmeddelanden
        uiState.errorMessage?.let { error ->
            Log.e("PreviewScreen", "Felmeddelande: $error")
            Text(
                text = "Fel: $error",
                modifier = Modifier.padding(16.dp),
                color = androidx.compose.ui.graphics.Color.Red
            )
        }

        // Om det finns resultat, skicka dem vidare
        if (uiState.result.isNotEmpty()) {
            Log.d("PreviewScreen", "Resultat hittat: ${uiState.result}")
            onSendComplete(uiState.result)
        }
        if (uiState.isLoading) {
            Text(
                text = "Analyserar bilden...",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}
