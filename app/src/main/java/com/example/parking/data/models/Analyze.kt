package com.example.parking.data.models


data class AnalyzeImageResponse(
    val description: Description?,
    val color: Color?,
    val tags: List<Tag>?,
    val objects: List<DetectedObject>?
)

data class Description(
    val captions: List<Caption>?
)

data class Caption(
    val text: String,
    val confidence: Float
)

data class Color(
    val dominantColors: List<String>?,
    val accentColor: String?
)

data class Tag(
    val name: String,
    val confidence: Float
)

data class DetectedObject(
    val objectProperty: String,
    val confidence: Float,
    val rectangle: Rectangle
)

data class Rectangle(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int
)

