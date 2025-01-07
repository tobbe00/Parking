package com.example.parking.data.repositories.azureocr

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AzureReadServiceFactory {

    private const val ENDPOINT = "https://tobbevision.cognitiveservices.azure.com/"
    private const val SUBSCRIPTION_KEY = "CtVeBp3FwRkGycUtJuu7jRULpopjL80QO0x7vVPD9AYAyqKL83UgJQQJ99ALACYeBjFXJ3w3AAAEACOGAWj1"

    fun create(): AzureReadService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(ENDPOINT)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AzureReadService::class.java)
    }
}

