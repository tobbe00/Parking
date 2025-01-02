package com.example.parking.data

import android.util.Log
import com.example.parking.data.azureocr.OcrLine
import com.example.parking.data.customvision.BoundingBox
import com.example.parking.data.customvision.Prediction


object PredictionProcessor {

    fun processPredictions(
        predictions: List<Prediction>,
        ocrLines: List<OcrLine>
    ): Map<String, Any> {
        // Filtrera prediktioner med sannolikhet ≥ 0.95
        val highConfidencePredictions = predictions.filter { it.probability >= 0.95 }
        Log.d("PredictionProcessor", "Predictions ≥ 95%: $highConfidencePredictions")

        // Matcha prediction med OCR-text
        val matchedPredictions = highConfidencePredictions.map { prediction ->
            val matchingText = ocrLines
                .filter { ocrLine -> isWithinBoundingBox(ocrLine.boundingBox, prediction.boundingBox) }
                .joinToString(separator = " ") { it.text }

            val customDescription = getCustomDescription(prediction.tagName)
            PredictionResult(
                tagName = prediction.tagName,
                text = matchingText,
                probability = prediction.probability,
                description = customDescription
            )
        }

        return mapOf(
            "MatchedPredictions" to matchedPredictions
        )
    }

    private fun isWithinBoundingBox(ocrBox: List<Pair<Float, Float>>, predictionBox: BoundingBox): Boolean {
        val (left, top, width, height) = predictionBox
        val right = left + width
        val bottom = top + height

        return ocrBox.all { (x, y) ->
            x in left..right && y in top..bottom
        }
    }

    private fun getCustomDescription(tagName: String): String {
        return when (tagName) {
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
    }

    data class PredictionResult(
        val tagName: String,
        val text: String,
        val probability: Float,
        val description: String
    )
}