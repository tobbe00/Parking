package com.example.parking.data.repositories.azureocr

import com.example.parking.data.models.ReadOperationResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface AzureReadService {


    @POST("vision/v3.2/read/analyze")
    @Headers("Content-Type: application/json")
    fun postImageUrl(
        @Body body: Map<String, String>
    ): Call<Void>


    @GET
    fun getReadResult(
        @Url operationLocation: String
    ): Call<ReadOperationResult>

}

