package com.example.parking.data.repositories.azureocr

import com.example.parking.data.models.ReadOperationResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface AzureReadService {

    // 1) POST (skicka in bild-URL i JSON-body) - får 202 Accepted och Operation-Location i Header
    @POST("vision/v3.2/read/analyze")
    @Headers("Content-Type: application/json")
    fun postImageUrl(
        @Body body: Map<String, String>  // { "url": "https://..." }
    ): Call<Void>  // Body är tom, men headers innehåller Operation-Location

    // 2) GET (pollar Operation-Location)
    @GET
    fun getReadResult(
        @Url operationLocation: String  // hela https://.../vision/v3.2/read/analyzeResults/<ID>
    ): Call<ReadOperationResult>

}

