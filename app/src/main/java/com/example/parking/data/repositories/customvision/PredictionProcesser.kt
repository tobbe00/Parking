package com.example.parking.data.repositories.customvision

import android.util.Log
import com.example.parking.data.models.OcrLine
import com.example.parking.data.models.TiderLineInfo
import com.example.parking.data.utils.getCustomDescription
import com.example.parking.data.utils.isTimeRange

object PredictionProcessor {

    /**
     * Returnerar en karta: "MatchedPredictions" -> List<PredictionResult>.
     *
     * Varje PredictionResult innehåller:
     * - tagName
     * - probability
     * - description (från getCustomDescription i TagNameMapper)
     * - boundingBox
     * - matchedOcrLines
     * - tiderLines (alla OCR-rader som är tidsintervall)
     * - text (alla OCR-rader som inte är tid, hopslagna till en sträng)
     */
    fun processPredictions(
        predictions: List<Prediction>,
        ocrLines: List<OcrLine>,
        imageWidth: Int,
        imageHeight: Int
    ): Map<String, Any> {
        if (imageWidth <= 0 || imageHeight <= 0) {
            throw IllegalArgumentException("Image width and height must be greater than 0")
        }

        Log.d("PredictionProcessor", "Alla detekterade tags:")
        predictions.forEach {
            Log.d("PredictionProcessor", "Tag: ${it.tagName}, Probability: ${it.probability}")
        }

        // 1) Normalisera OCR-linjerna
        val normalizedOcrLines = ocrLines.map { line ->
            val normBox = line.pixelBox.mapIndexed { i, value ->
                if (i % 2 == 0) value / imageWidth else value / imageHeight
            }
            line.copy(normalizedBox = normBox)
        }

        // 2) Filtrera predictions med sannolikhet >= 0.8 (justera tröskel själv)
        val highConfidencePredictions = predictions.filter { it.probability >= 0.8 }

        // 3) Håll endast den högst sannolika predictionen per tagg
        val uniquePredictions = highConfidencePredictions
            .groupBy { it.tagName }
            .mapValues { (_, predictions) ->
                predictions.maxByOrNull { it.probability }!!
            }
            .values

        // 4) Matcha varje unik prediction med OCR-linjer
        val matchedPredictions = uniquePredictions.map { prediction ->
            // Hitta OCR-linjer vars bounding box överlappar
            val matchedLines = normalizedOcrLines.filter { ocrLine ->
                val ocrBox = ocrLine.normalizedBox
                val cvBox = prediction.boundingBox
                cvBox.left <= ocrBox[2] &&
                        (cvBox.left + cvBox.width) >= ocrBox[0] &&
                        cvBox.top <= ocrBox[3] &&
                        (cvBox.top + cvBox.height) >= ocrBox[1]
            }

            // Dela upp i "tidsintervall" vs. "övriga rader"
            val timeLines = matchedLines.filter { isTimeRange(it.text) }
            val otherLines = matchedLines.filter { !isTimeRange(it.text) }

            // Bygg TiderLineInfo av tidsrader
            val tiderLineInfos = timeLines.map { line ->
                TiderLineInfo(
                    text = line.text,
                    isRed = false
                )
            }

            // Övriga rader slår vi ihop till en sträng
            val matchingText = otherLines.joinToString(", ") { it.text }

            // Bygg ut PredictionResult
            PredictionResult(
                tagName = prediction.tagName,
                text = matchingText,
                probability = prediction.probability,
                description = getCustomDescription(prediction.tagName),
                boundingBox = prediction.boundingBox,
                matchedOcrLines = matchedLines,
                tiderLines = tiderLineInfos
            )
        }

        return mapOf("MatchedPredictions" to matchedPredictions)
    }


    // Data class för “slutresultatet”
    data class PredictionResult(
        val tagName: String,
        val text: String,
        val probability: Float,
        val description: String,
        val boundingBox: BoundingBox,
        val matchedOcrLines: List<OcrLine>,
        val tiderLines: List<TiderLineInfo> = emptyList()
    )
}
