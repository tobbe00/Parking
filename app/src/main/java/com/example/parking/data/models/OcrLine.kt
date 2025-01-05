package com.example.parking.data.models

/**
 * @property pixelBox  - Azure OCR-returnerade koordinater i pixlar (ex [x1, y1, x2, y2, x3, y3, x4, y4])
 * @property normalizedBox - Samma box men normaliserad ([0..1]) för att matcha mot Custom Vision bounding boxar.
 */
data class OcrLine(
    val text: String,
    val pixelBox: List<Float>,
    val normalizedBox: List<Float> = emptyList()
)
