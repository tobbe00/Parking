package com.example.parking.data.azureocr

import android.util.Log
import com.example.parking.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

object AzureReadRepository {

    private val service = AzureReadServiceFactory.create()

    /**
     * Anropar Read API med en bild-URL, pollar Operation-Location tills "succeeded" eller "failed".
     * Returnerar en lista med OCR-linjer (text + bounding box).
     */
    suspend fun getTextFromImageUrl(imageUrl: String): List<OcrLine> = withContext(Dispatchers.IO) {
        try {
            // 1) Skicka in bilden (URL) till "read/analyze"
            val postResp: Response<Void> = service.postImageUrl(mapOf("url" to imageUrl)).execute()
            if (!postResp.isSuccessful) {
                val errorBody = postResp.errorBody()?.string()
                Log.e("AzureReadRepository", "Fel i POST: ${postResp.code()} - $errorBody")
                return@withContext emptyList()
            }

            // Få ut Operation-Location från header
            val operationLocation = postResp.headers()["Operation-Location"]
            if (operationLocation.isNullOrBlank()) {
                Log.e("AzureReadRepository", "Saknar Operation-Location i POST-svaret")
                return@withContext emptyList()
            }

            // 2) Polla GET tills status=succeeded
            var readResult: ReadOperationResult? = null
            for (i in 1..10) {
                delay(1000L) // vänta 1 sekund
                val getResp = service.getReadResult(operationLocation).execute()
                if (getResp.isSuccessful) {
                    val body = getResp.body()
                    if (body?.status == "succeeded") {
                        readResult = body
                        break
                    } else if (body?.status == "failed") {
                        Log.e("AzureReadRepository", "OCR failed enligt status=failed")
                        break
                    }
                } else {
                    throw HttpException(getResp)
                }
            }

            // 3) Extrahera text och bounding boxar
            val ocrLines = mutableListOf<OcrLine>()
            readResult?.analyzeResult?.readResults?.forEach { page ->
                page.lines?.forEach { line ->
                    val boundingBox = line.boundingBox.chunked(2).map { it[0] to it[1] }
                    ocrLines.add(
                        OcrLine(
                            text = line.text,
                            boundingBox = boundingBox
                        )
                    )
                }
            }
            return@withContext ocrLines

        } catch (e: Exception) {
            Log.e("AzureReadRepository", "getTextFromImageUrl Exception: ${e.message}", e)
            return@withContext emptyList()
        }
    }
}

/**
 * Representerar en OCR-linje med text och bounding box.
 */
data class OcrLine(
    val text: String,
    val boundingBox: List<Pair<Float, Float>>
)