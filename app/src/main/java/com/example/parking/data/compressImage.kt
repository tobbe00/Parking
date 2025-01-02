package com.example.parking.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

fun compressImage(imagePath: String, context: Context): File {
    // Kontrollera att originalfilen existerar
    val originalFile = File(imagePath)
    if (!originalFile.exists() || originalFile.length() == 0L) {
        throw FileNotFoundException("Originalfilen hittades inte eller är tom: $imagePath")
    }

    // Ladda bilden från den givna sökvägen
    val bitmap = BitmapFactory.decodeFile(imagePath)
    if (bitmap == null) {
        throw IllegalArgumentException("Kunde inte läsa bitmap från fil: $imagePath")
    }

    // Skapa en ny temporär komprimerad fil
    val compressedFile = File(context.cacheDir, "compressed_temp_image.jpg")
    if (compressedFile.exists()) {
        compressedFile.delete() // Radera eventuell tidigare version
    }

    // Komprimera och spara till filen
    var quality = 90 // Startar med hög kvalitet
    FileOutputStream(compressedFile).use { outputStream ->
        do {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            quality -= 5 // Minska kvaliteten gradvis
        } while (compressedFile.length() > 4 * 1024 * 1024 && quality > 10) // 4 MB-gräns
    }

    // Kontrollera att filen skapades och är giltig
    if (!compressedFile.exists() || compressedFile.length() == 0L) {
        throw FileNotFoundException("Den komprimerade filen kunde inte skapas eller är tom: ${compressedFile.absolutePath}")
    }

    return compressedFile
}
