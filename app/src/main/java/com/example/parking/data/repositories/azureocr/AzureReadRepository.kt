package com.example.parking.data.repositories.azureocr

import android.util.Log
import com.example.parking.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

object AzureReadRepository {

    private val service = AzureReadServiceFactory.create()

    suspend fun getOcrLines(imageUrl: String): Pair<List<OcrLine>, Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {

            val postResp: Response<Void> = service.postImageUrl(mapOf("url" to imageUrl)).execute()
            if (!postResp.isSuccessful) {
                Log.e("AzureReadRepository", "POST-fel: ${postResp.code()} - ${postResp.errorBody()?.string()}")
                return@withContext Pair(emptyList(), Pair(0, 0))
            }


            val operationLocation = postResp.headers()["Operation-Location"]
            if (operationLocation.isNullOrBlank()) {
                Log.e("AzureReadRepository", "Saknar Operation-Location")
                return@withContext Pair(emptyList(), Pair(0, 0))
            }

            var readResult: ReadOperationResult? = null
            for (i in 1..10) {
                delay(1000L)
                val getResp = service.getReadResult(operationLocation).execute()
                if (getResp.isSuccessful) {
                    val body = getResp.body()
                    if (body?.status == "succeeded") {
                        readResult = body
                        break
                    } else if (body?.status == "failed") {
                        Log.e("AzureReadRepository", "OCR misslyckades med status=failed")
                        break
                    }
                } else {
                    throw HttpException(getResp)
                }
            }

            val ocrLines = mutableListOf<OcrLine>()
            var width = 0
            var height = 0

            readResult?.analyzeResult?.readResults?.firstOrNull()?.let { page ->
                width = page.width
                height = page.height
                page.lines?.forEach { line ->
                    // här sparar vi pixelBox som "pixelBox"
                    ocrLines.add(
                        OcrLine(
                            text = line.text,
                            pixelBox = line.boundingBox // i PIXLAR från Azure OCR
                        )
                    )
                }
            }

            return@withContext Pair(ocrLines, Pair(width, height))

        } catch (e: Exception) {
            Log.e("AzureReadRepository", "Undantag i getOcrLines: ${e.message}", e)
            return@withContext Pair(emptyList(), Pair(0, 0))
        }
    }
}
