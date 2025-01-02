// ParkingVisionRepository.kt
package com.example.parking.data.customvision

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

object ParkingVisionRepository {

    private val service = ParkingVisionServiceFactory.create()

    /**
     * Anropa Custom Vision detect/iterations/.../url med en bild-URL
     * Returnerar en CustomVisionResponse som innehåller "predictions" (tagName, boundingBox m.m.)
     */
    suspend fun detectFromImageUrl(imageUrl: String): CustomVisionResponse? = withContext(Dispatchers.IO) {
        try {
            val resp = service.detectFromUrl(mapOf("Url" to imageUrl)).execute()
            if (resp.isSuccessful) {
                return@withContext resp.body()
            } else {
                val errorBody = resp.errorBody()?.string()
                Log.e("ParkingVisionRepo", "Fel i Custom Vision: ${resp.code()} - $errorBody")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("ParkingVisionRepo", "Exception i detectFromImageUrl: ${e.message}", e)
            return@withContext null
        }
    }
}
