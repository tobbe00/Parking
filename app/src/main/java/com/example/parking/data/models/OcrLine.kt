package com.example.parking.data.models

data class OcrLine(
    val text: String,
    val pixelBox: List<Float>,
    val normalizedBox: List<Float> = emptyList()
)
