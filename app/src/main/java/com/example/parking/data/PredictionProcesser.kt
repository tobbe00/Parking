package com.example.parking.data

import android.util.Log
import com.example.parking.data.models.OcrLine
import com.example.parking.data.customvision.BoundingBox
import com.example.parking.data.customvision.Prediction

object PredictionProcessor {

    private const val OCR_IMAGE_WIDTH = 1450.0
    private const val OCR_IMAGE_HEIGHT = 3300.0

    fun processPredictions(
        predictions: List<Prediction>,
        ocrLines: List<OcrLine>,
        imageWidth: Int,
        imageHeight: Int
    ): Map<String, Any> {
        Log.d("PredictionProcessor", "Startar bearbetning av predictions...")

        if (imageWidth <= 0 || imageHeight <= 0) {
            Log.e("PredictionProcessor", "Ogiltiga bilddimensioner: width=$imageWidth, height=$imageHeight")
            throw IllegalArgumentException("Image width and height must be greater than 0")
        }

        Log.d("PredictionProcessor", "Bildens dimensioner: width=$imageWidth, height=$imageHeight")

        // Skala OCR-koordinaterna
        val scaledOcrLines = ocrLines.map { ocrLine ->
            val boundingBox = ocrLine.boundingBox
            val scaledBoundingBox = boundingBox.mapIndexed { index, value ->
                if (index % 2 == 0) {
                    (value / OCR_IMAGE_WIDTH * imageWidth).toFloat() // Skala X-koordinater och konvertera till Float
                } else {
                    (value / OCR_IMAGE_HEIGHT * imageHeight).toFloat() // Skala Y-koordinater och konvertera till Float
                }
            }
            ocrLine.copy(boundingBox = scaledBoundingBox)
        }

        scaledOcrLines.forEach { scaledOcrLine ->
            Log.d("PredictionProcessor", "Scaled OCR Line: ${scaledOcrLine.text}, BoundingBox: ${scaledOcrLine.boundingBox}")
        }

        // Filtrera prediktioner med sannolikhet ≥ 0.95
        val highConfidencePredictions = predictions.filter { it.probability >= 0.95 }
        Log.d("PredictionProcessor", "Predictions ≥ 95%: $highConfidencePredictions")

        // Matcha prediction med OCR-text
        val matchedPredictions = highConfidencePredictions.map { prediction ->
            Log.d("PredictionProcessor", "Bearbetar prediction: ${prediction.tagName}, BoundingBox: ${prediction.boundingBox}")

            val matchingText = scaledOcrLines.filter { ocrLine ->
                Log.d("PredictionProcessor", "OCR Line: ${ocrLine.text}, BoundingBox: ${ocrLine.boundingBox}")

                // Kontrollera om OCR och prediction-boxar överlappar
                val predictionBox = prediction.boundingBox
                val ocrBox = ocrLine.boundingBox
                val isMatch = predictionBox.left <= ocrBox[2] && predictionBox.left + predictionBox.width >= ocrBox[0] &&
                        predictionBox.top <= ocrBox[3] && predictionBox.top + predictionBox.height >= ocrBox[1]

                Log.d("PredictionProcessor", "BoundingBox-kontroll: OCR Box: ${ocrBox}, Prediction Box: (${predictionBox.left}, ${predictionBox.top}, ${predictionBox.left + predictionBox.width}, ${predictionBox.top + predictionBox.height}), Is Match: $isMatch")
                isMatch
            }.map { it.text }

            Log.d("PredictionProcessor", "Resultat för prediction: ${prediction.tagName}, Matching Texts: $matchingText")

            val customDescription = getCustomDescription(prediction.tagName)
            PredictionResult(
                tagName = prediction.tagName,
                text = matchingText.joinToString(", "), // Slår ihop listan till en sträng
                probability = prediction.probability,
                description = customDescription
            )
        }

        Log.d("PredictionProcessor", "Matched Predictions: $matchedPredictions")
        return mapOf("MatchedPredictions" to matchedPredictions)
    }

    private fun getCustomDescription(tagName: String): String {
        val description = when (tagName) {
            "FörbudJämnUdda" -> "Förbjudet att parkera på ena sidan beroende på om datumet är jämnt eller udda"
            "FörbudJämn" -> "Förbjudet att parkera på dag med jämnt datum"
            "FörbudUdda" -> "Förbjudet att parkera på dag med udda datum"
            "FörbudStop" -> "Förbjudet att stanna"
            "FörbudParkera" -> "Förbjudet att parkera"
            "Huvudled" -> "Förbjudet att parkera på en väg som är huvudled"
            "HögerVänsterPil" -> "Parkering till båda sidor om skylten"
            "pilVänster" -> "Parkering till vänster om skylten"
            "pilHöger" -> "Parkering till höger om skylten"
            "Laddplats" -> "Endast elbilar får parkera"
            "P-skiva" -> "Måste använda P-skiva"
            "RakParkering" -> "Parkera vinkelrätt"
            else -> "Okänd skylt"
        }
        Log.d("PredictionProcessor", "Custom Description för $tagName: $description")
        return description
    }

    data class PredictionResult(
        val tagName: String,
        val text: String,
        val probability: Float,
        val description: String
    )
}