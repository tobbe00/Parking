package com.example.parking.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log

object ColorDetectionUtils {

    fun loadBitmapFromUri(context: Context, imageUri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(imageUri).use { inputStream ->
                if (inputStream != null) {
                    BitmapFactory.decodeStream(inputStream)
                } else null
            }
        } catch (e: Exception) {
            Log.e("ColorDetectionUtils", "loadBitmapFromUri error: ${e.message}", e)
            null
        }
    }

    /**
     * Anropas med pixel-koordinater (x1,y1,x2,y2,x3,y3,x4,y4) i pixlar från OCR-linjen.
     * Denna funktion beskär en inner-rektangel och kollar om en stor andel av pixlarna är "röda".
     * Nu använder vi en HSV-baserad metod, vilket funkar bättre mot gul eller blå bakgrund.
     */
    fun isTextRedForOcrLinePixels(
        originalBitmap: Bitmap,
        pixelBox: List<Float>
    ): Boolean {

        // 1) Säkerställ att vi har minst 8 värden (4 hörn)
        if (pixelBox.size < 8) return false

        // Hämta min/max X och Y
        val xs = listOf(pixelBox[0], pixelBox[2], pixelBox[4], pixelBox[6])
        val ys = listOf(pixelBox[1], pixelBox[3], pixelBox[5], pixelBox[7])

        val minX = xs.minOrNull()?.toInt() ?: 0
        val maxX = xs.maxOrNull()?.toInt() ?: 0
        val minY = ys.minOrNull()?.toInt() ?: 0
        val maxY = ys.maxOrNull()?.toInt() ?: 0

        val w = maxX - minX
        val h = maxY - minY
        if (w <= 0 || h <= 0) return false

        // Clamp - se till att vi inte går utanför bilden
        val clampedLeft = minX.coerceIn(0, originalBitmap.width - 1)
        val clampedTop = minY.coerceIn(0, originalBitmap.height - 1)
        val clampedWidth = w.coerceIn(0, originalBitmap.width - clampedLeft)
        val clampedHeight = h.coerceIn(0, originalBitmap.height - clampedTop)
        if (clampedWidth <= 0 || clampedHeight <= 0) return false

        // 2) Krymp rektangeln med ~10% för att undvika skarpa kanter
        val marginFactor = 0.1
        val innerLeft = (clampedLeft + clampedWidth * marginFactor).toInt()
        val innerTop = (clampedTop + clampedHeight * marginFactor).toInt()
        val innerWidth = (clampedWidth * (1 - 2 * marginFactor)).toInt()
        val innerHeight = (clampedHeight * (1 - 2 * marginFactor)).toInt()
        Log.d("ColorDetectionUtils", "minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY, w=$w, h=$h")
        Log.d("ColorDetectionUtils", "innerLeft=$innerLeft, innerTop=$innerTop, innerWidth=$innerWidth, innerHeight=$innerHeight")

        if (innerWidth <= 0 || innerHeight <= 0) return false

        // 3) Skapa en beskuren bitmap av den inre rektangeln
        val croppedBitmap = Bitmap.createBitmap(
            originalBitmap,
            innerLeft,
            innerTop,
            innerWidth,
            innerHeight
        )

        // 4) Kolla hur stor andel av pixlarna som är "röda" enligt HSV
        return isTextRedPixelRatioHSV(croppedBitmap)
    }

    /**
     * Räknar hur många pixlar som ligger i ett “rött” HSV-område.
     * Ex: hue < 20 eller hue > 340, saturationen > 0.4, value > 0.2
     * Om mer än 20% av pixlarna är röda => vi anser att texten är röd.
     */
    private fun isTextRedPixelRatioHSV(bitmap: Bitmap): Boolean {
        var redCount = 0
        val width = bitmap.width
        val height = bitmap.height
        val pixelCount = width * height

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Konvertera till HSV
                val hsv = FloatArray(3)
                Color.RGBToHSV(r, g, b, hsv)

                val hue = hsv[0]   // [0..360)
                val sat = hsv[1]   // [0..1]
                val value = hsv[2] // [0..1]

                // En enkel "röd" definition:
                //   hue < 20 eller hue > 340
                //   sat > 0.4
                //   value > 0.2
                if ((hue < 30f || hue > 330f) && sat > 0.3f && value > 0.1f) {
                    redCount++
                }
            }
        }

        val ratio = redCount.toDouble() / pixelCount.toDouble()
        Log.d("ColorDetectionUtils", "Red pixel ratio = $ratio")
        val coverageThreshold = 0.15 // 20%
        return ratio > coverageThreshold
    }
}
