package com.example.parking.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

import com.example.parking.ui.viewmodels.ParkingViewModel

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

        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Captured Image",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentScale = ContentScale.Crop
        )


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

        uiState.errorMessage?.let { error ->
            Log.e("PreviewScreen", "Felmeddelande: $error")
            Text(
                text = "Fel: $error",
                modifier = Modifier.padding(16.dp),
                color = androidx.compose.ui.graphics.Color.Red
            )
        }

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
