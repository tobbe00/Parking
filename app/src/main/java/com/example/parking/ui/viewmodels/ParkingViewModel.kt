package com.example.parking.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parking.data.azureocr.AzureReadRepository
import com.example.parking.data.customvision.ParkingVisionRepository
import kotlinx.coroutines.launch
import com.example.parking.data.*
import com.example.parking.data.models.ParkingUiState


class ParkingViewModel : ViewModel() {

    var uiState by mutableStateOf(ParkingUiState())
        private set

    fun sendImage(context: Context, imageUri: Uri?, sasUrl: String) {
        if (imageUri == null) {
            Log.e("ParkingViewModel", "Image URI är null. Avbryter.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ParkingViewModel", "Laddar upp bilden...")
                val blobUrl = uploadImageWithSasToken(context, imageUri, sasUrl)

                Log.d("ParkingViewModel", "Hämtar OCR-data...")
                val ocrLines = AzureReadRepository.getTextFromImageUrl(blobUrl) // OCR med bounding boxar

                Log.d("ParkingViewModel", "Analyserar bild med Custom Vision...")
                val customVisionResult = ParkingVisionRepository.detectFromImageUrl(blobUrl)
                val predictions = customVisionResult?.predictions ?: emptyList()

                Log.d("ParkingViewModel", "Bearbetar predictions och matchar med OCR...")
                val processedResults = PredictionProcessor.processPredictions(predictions, ocrLines)

                Log.d("ParkingViewModel", "Processed Results: $processedResults")

                // Uppdatera UI-state
                uiState = uiState.copy(
                    result = processedResults,
                    errorMessage = null
                )

            } catch (e: Exception) {
                Log.e("ParkingViewModel", "Fel vid bildanalys: ${e.message}", e)
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }
    // Method to clear results and reset the state
    fun clearResults() {
        Log.d("ParkingViewModel", "Clearing results and resetting UI state.")
        uiState = ParkingUiState() // Resets the UI state to its initial value
    }
}