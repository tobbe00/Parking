package com.example.parking.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.parking.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import android.util.Base64
import android.util.Log
import com.example.parking.getGoogleCredentials
import okhttp3.OkHttpClient

suspend fun callVisionApi(context: Context, imageUri: Uri): List<String> {
    return withContext(Dispatchers.IO) {
        try {
            // Konvertera bilden till Base64
            val encodedImage = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                Base64.encodeToString(inputStream.readBytes(), Base64.DEFAULT)
            } ?: throw IllegalArgumentException("Kunde inte läsa bilden från URI: $imageUri")

            // Läs autentisering från GoogleCredentials
            val credentials = getGoogleCredentials(context)
            val token = credentials.refreshAccessToken().tokenValue
            Log.d("VisionAPI", "Access Token hämtat: $token")

            // Förbered Vision API-anropet
            val visionRequest = VisionRequest(
                requests = listOf(
                    Request(
                        image = Image(content = encodedImage),
                        features = listOf(
                            Feature(type = "TEXT_DETECTION", maxResults = 10)
                        )
                    )
                )
            )

            val retrofit = Retrofit.Builder()
                .baseUrl("https://vision.googleapis.com/")
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                            chain.proceed(request)
                        }.build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(VisionApiService::class.java)

            // Skicka förfrågan och hantera svar
            val response = service.analyzeImage(visionRequest).execute()
            if (response.isSuccessful) {
                // Hämta endast textresultat
                val textAnnotations = response.body()?.responses?.firstOrNull()?.textAnnotations
                textAnnotations?.map { it.description } ?: emptyList()
            } else {
                val error = response.errorBody()?.string()
                Log.e("VisionAPI", "API-fel: $error")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("VisionAPI", "Ett undantag inträffade: ${e.message}", e)
            emptyList()
        }
    }
}
