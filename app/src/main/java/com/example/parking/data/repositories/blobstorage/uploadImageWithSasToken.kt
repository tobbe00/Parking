package com.example.parking.data.repositories.blobstorage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.parking.data.utils.compressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

suspend fun uploadImageWithSasToken(
    context: Context,
    uri: Uri,
    sasUrl: String,
): String = withContext(Dispatchers.IO) {
    try {
        Log.d("BlobUpload", "=== Startar uppladdning med SAS-token ===")
        Log.d("BlobUpload", "URI: $uri")


        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Kunde inte läsa bilden från URI: $uri")
        val tempFile = File(context.cacheDir, "temp_image.jpg")
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        Log.d("BlobUpload", "Bilden sparad till temporär fil: ${tempFile.absolutePath} (Storlek: ${tempFile.length()} bytes)")


        val compressedFile = compressImage(tempFile.absolutePath, context)
        Log.d(
            "BlobUpload",
            "Komprimerad bild sparad: ${compressedFile.absolutePath} (Storlek: ${compressedFile.length()} bytes)"
        )

        val blobName = "${System.currentTimeMillis()}.jpg"

        val baseUrl = sasUrl.substringBefore("?")
        val sasToken = sasUrl.substringAfter("?")
        val uploadUrl = "$baseUrl/$blobName?$sasToken"

        Log.d("BlobUpload", "Uppladdnings-URL: $uploadUrl")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(uploadUrl)
            .put(compressedFile.asRequestBody("image/jpeg".toMediaType()))
            .addHeader("x-ms-blob-type", "BlockBlob")
            .build()

        Log.d("BlobUpload", "Skickar PUT-förfrågan till Blob Storage...")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(
                    "BlobUpload",
                    "Fel vid uppladdning: ${response.code} - ${response.message}, Body: $errorBody"
                )
                throw Exception("Failed to upload image: ${response.code} - ${response.message}")
            } else {
                Log.d("BlobUpload", "Uppladdning lyckades. HTTP ${response.code}")
            }
        }

        Log.d("BlobUpload", "Uppladdningen lyckades, filens URL: $uploadUrl")
        uploadUrl
    } catch (e: Exception) {
        Log.e("BlobUpload", "Ett fel inträffade: ${e.message}", e)
        throw e
    }
}
