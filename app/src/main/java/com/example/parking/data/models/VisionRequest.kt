package com.example.parking.data.models

data class VisionRequest(
    val requests: List<Request>
)

data class Request(
    val image: Image,
    val features: List<Feature>
)

data class Image(
    val content: String // Base64-kodad bild
)

data class Feature(
    val type: String,
    val maxResults: Int
)
