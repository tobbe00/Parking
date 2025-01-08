package com.example.parking.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

fun compressImage(imagePath: String, context: Context): File {

    val originalFile = File(imagePath)
    if (!originalFile.exists() || originalFile.length() == 0L) {
        throw FileNotFoundException("Originalfilen hittades inte eller är tom: $imagePath")
    }


    val bitmap = BitmapFactory.decodeFile(imagePath)
    if (bitmap == null) {
        throw IllegalArgumentException("Kunde inte läsa bitmap från fil: $imagePath")
    }


    val compressedFile = File(context.cacheDir, "compressed_temp_image.jpg")
    if (compressedFile.exists()) {
        compressedFile.delete()
    }


    var quality = 90
    FileOutputStream(compressedFile).use { outputStream ->
        do {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            quality -= 5
        } while (compressedFile.length() > 4 * 1024 * 1024 && quality > 10) // 4 MB-gräns
    }


    if (!compressedFile.exists() || compressedFile.length() == 0L) {
        throw FileNotFoundException("Den komprimerade filen kunde inte skapas eller är tom: ${compressedFile.absolutePath}")
    }

    return compressedFile
}
