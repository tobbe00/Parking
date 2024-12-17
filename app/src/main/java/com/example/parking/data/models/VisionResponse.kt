package com.example.parking.data.models

data class VisionResponse(
    val responses: List<Response>
)

data class Response(
    val labelAnnotations: List<LabelAnnotation>?,
    val textAnnotations: List<TextAnnotation>?
)

data class LabelAnnotation(
    val description: String,
    val score: Float
)

data class TextAnnotation(
    val description: String,
    val boundingPoly: BoundingPoly?
)

data class BoundingPoly(
    val vertices: List<Vertex>
)

data class Vertex(
    val x: Int?,
    val y: Int?
)
