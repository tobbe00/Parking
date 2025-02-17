package com.example.parking.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parking.data.repositories.azureocr.AzureReadRepository
import com.example.parking.data.repositories.customvision.ParkingVisionRepository
import com.example.parking.data.utils.ColorDetectionUtils
import com.example.parking.data.repositories.customvision.PredictionProcessor
import com.example.parking.data.repositories.blobstorage.uploadImageWithSasToken
import com.example.parking.data.models.ParkingUiState
import com.example.parking.data.models.TiderLineInfo
import kotlinx.coroutines.launch

class ParkingViewModel : ViewModel() {

    var uiState by mutableStateOf(ParkingUiState())
        private set

    fun sendImage(context: Context, imageUri: Uri?, sasUrl: String) {
        if (imageUri == null) {
            Log.e("ParkingViewModel", "Bild-URI är null. Avbryter.")
            return
        }

        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true)
                Log.d("ParkingViewModel", "Laddar upp bilden...")
                val blobUrl = uploadImageWithSasToken(context, imageUri, sasUrl)


                val originalBitmap = ColorDetectionUtils.loadBitmapFromUri(context, imageUri)
                if (originalBitmap == null) {
                    Log.e("ParkingViewModel", "Kunde inte läsa in originalbilden lokalt.")
                }


                Log.d("ParkingViewModel", "Hämtar OCR-data...")
                val (ocrLines, dimensions) = AzureReadRepository.getOcrLines(blobUrl)
                val (ocrWidth, ocrHeight) = dimensions

                if (ocrWidth == 0 || ocrHeight == 0) {
                    Log.e("ParkingViewModel", "Ogiltiga OCR-dimensioner.")
                    uiState = uiState.copy(errorMessage = "OCR-dimensioner är ogiltiga.")
                    return@launch
                }

                Log.d("ParkingViewModel", "Analyserar bild med Custom Vision...")
                val customVisionResult = ParkingVisionRepository.detectFromImageUrl(blobUrl)
                val predictions = customVisionResult?.predictions ?: emptyList()


                Log.d("ParkingViewModel", "Bearbetar predictions och matchar med OCR...")
                val processedResults = PredictionProcessor.processPredictions(
                    predictions,
                    ocrLines,
                    ocrWidth,
                    ocrHeight
                )
                val matchedPredictions = processedResults["MatchedPredictions"]
                        as? List<PredictionProcessor.PredictionResult> ?: emptyList()


                val updatedPredictions = matchedPredictions.map { pr ->
                    if (originalBitmap != null) {
                        val lineInfos = pr.matchedOcrLines.map { ocrLine ->
                            Log.d("ColorDetectionUtils", "Nu anropar jag isTextRedForOcrLinePixels för ${ocrLine.text}")
                            val isRed = ColorDetectionUtils.isTextRedForOcrLinePixels(
                                originalBitmap,
                                ocrLine.pixelBox
                            )
                            Log.d("ColorDetectionUtils", "Resultat = $isRed")
                            TiderLineInfo(text = ocrLine.text, isRed = isRed)
                        }
                        pr.copy(tiderLines = lineInfos)
                    } else pr
                }

                uiState = uiState.copy(
                    result = mapOf("MatchedPredictions" to updatedPredictions),
                    errorMessage = null,
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }


    fun clearResults() {
        uiState = ParkingUiState()
    }
}


fun PredictionProcessor.PredictionResult.copy(
    tiderLines: List<TiderLineInfo>
): PredictionProcessor.PredictionResult {
    return PredictionProcessor.PredictionResult(
        tagName = this.tagName,
        text = this.text,
        probability = this.probability,
        description = this.description,
        boundingBox = this.boundingBox,
        matchedOcrLines = this.matchedOcrLines,
        tiderLines = tiderLines
    )
}