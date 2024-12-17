package com.example.parking.data

import com.example.parking.data.models.VisionRequest
import com.example.parking.data.models.VisionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VisionApiService {
    @POST("v1/images:annotate")
    fun analyzeImage(
        @Body requestBody: VisionRequest
    ): Call<VisionResponse>
}

