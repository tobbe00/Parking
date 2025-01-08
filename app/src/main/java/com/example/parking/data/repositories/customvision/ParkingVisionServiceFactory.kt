// ParkingVisionServiceFactory.kt
package com.example.parking.data.repositories.customvision

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ParkingVisionServiceFactory {

    private const val BASE_URL = "https://parkingvision-prediction.cognitiveservices.azure.com/"

    fun create(): ParkingVisionService {

        val client = OkHttpClient.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ParkingVisionService::class.java)
    }
}
