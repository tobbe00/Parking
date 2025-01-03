package com.example.parking.data.models

data class OcrLine(
    val text: String,
    val boundingBox: List<Float>
)