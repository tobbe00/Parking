package com.example.parking.data.repositories.customvision

import android.util.Log
import com.example.parking.data.models.OcrLine
import com.example.parking.data.models.TiderLineInfo
import com.example.parking.data.utils.getCustomDescription
import com.example.parking.data.utils.isTimeRange

object PredictionProcessor {


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


        val normalizedOcrLines = ocrLines.map { line ->
            val normBox = line.pixelBox.mapIndexed { i, value ->
                if (i % 2 == 0) value / imageWidth else value / imageHeight
            }
            line.copy(normalizedBox = normBox)
        }


        val highConfidencePredictions = predictions.filter { it.probability >= 0.8 }


        val uniquePredictions = highConfidencePredictions
            .groupBy { it.tagName }
            .mapValues { (_, predictions) ->
                predictions.maxByOrNull { it.probability }!!
            }
            .values


        val matchedPredictions = uniquePredictions.map { prediction ->

            val matchedLines = normalizedOcrLines.filter { ocrLine ->
                val ocrBox = ocrLine.normalizedBox
                val cvBox = prediction.boundingBox
                cvBox.left <= ocrBox[2] &&
                        (cvBox.left + cvBox.width) >= ocrBox[0] &&
                        cvBox.top <= ocrBox[3] &&
                        (cvBox.top + cvBox.height) >= ocrBox[1]
            }

            val timeLines = matchedLines.filter { isTimeRange(it.text) }
            val otherLines = matchedLines.filter { !isTimeRange(it.text) }


            val tiderLineInfos = timeLines.map { line ->
                TiderLineInfo(
                    text = line.text,
                    isRed = false
                )
            }

            val matchingText = otherLines.joinToString(", ") { it.text }

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
