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
                val ocrLines = AzureReadRepository.getTextFromImageUrl(blobUrl)

                Log.d("ParkingViewModel", "Analyserar bild med Custom Vision...")
                val customVisionResult = ParkingVisionRepository.detectFromImageUrl(blobUrl)
                val predictions = customVisionResult?.predictions ?: emptyList()

                // Hämta bildstorlek (dummyvärden, byt ut vid behov)
                val imageWidth = 1920 // Exempel på bredd
                val imageHeight = 1080 // Exempel på höjd

                Log.d("ParkingViewModel", "Bearbetar predictions och matchar med OCR...")
                val processedResults = PredictionProcessor.processPredictions(
                    predictions = predictions,
                    ocrLines = ocrLines,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )

                Log.d("ParkingViewModel", "Processed Results: $processedResults")

                val matchedPredictions = processedResults["MatchedPredictions"] as? List<PredictionProcessor.PredictionResult>
                val ocrOnlyLines = ocrLines.map { it.text }

                uiState = uiState.copy(
                    result = mapOf(
                        "MatchedPredictions" to matchedPredictions.orEmpty(),
                        "OcrLines" to ocrOnlyLines
                    ),
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("ParkingViewModel", "Fel vid bildanalys: ${e.message}", e)
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }
}
