package com.example.parking.data.repositories.customvision

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Exempel på Custom Vision-svar:
data class CustomVisionResponse(
    val id: String?,
    val project: String?,
    val iteration: String?,
    val created: String?,
    val predictions: List<Prediction>?
)

data class Prediction(
    val probability: Float,
    val tagId: String,
    val tagName: String,
    val boundingBox: BoundingBox
)

data class BoundingBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

interface ParkingVisionService {

    @POST("customvision/v3.0/Prediction/aaaf6b69-b0fb-451c-bfc1-fdf41b3672ae/detect/iterations/Iteration5/url")
    @Headers(
        "Content-Type: application/json",
        "Prediction-Key: peaJn2umHlW71bsOzZ0eTdddP5bjsXai0TsjXEZPnfm95tIBTWMFJQQJ99ALACYeBjFXJ3w3AAAIACOG8cFQ"
    )
    fun detectFromUrl(
        @Body body: Map<String, String> // { "Url": "https://..." }
    ): Call<CustomVisionResponse>
}
