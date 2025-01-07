package com.example.parking.data.utils

import android.util.Log

fun getCustomDescription(tagName: String): String {
    val description = when (tagName) {
        "FörbudJämnUdda" -> "Parkering är förbjuden på sidan med udda husnummer på udda datum och på sidan med jämna husnummer på jämna datum."
        "FörbudJämn" -> "Förbjudet att parkera på dag med jämnt datum"
        "FörbudUdda" -> "Förbjudet att parkera på dag med udda datum"
        "FörbudStop" -> "Förbjudet att stanna"
        "FörbudParkera" -> "Förbjudet att parkera"
        "Huvudled" -> "Normalt förbjudet att parkera vid huvudled, men tillåtet om P-skylt finns"
        "HögerVänsterPil" -> "Parkering till båda sidor om skylten"
        "Pil vänster" -> "Parkering till vänster om skylten"
        "Pil höger" -> "Parkering till höger om skylten"
        "Laddplats" -> "Endast elbilar får parkera"
        "P-skiva" -> "Måste använda P-skiva"
        "P-symbol" -> "Man får parkera"
        "RakParkering" -> "Parkera vinkelrätt"
        else -> "Okänd skylt"
    }
    Log.d("PredictionProcessor", "Custom Description för $tagName: $description")
    return description
}

fun mapTagNameToDisplayName(tagName: String): String {
    return when (tagName) {
        "FörbudJämn" -> "Förbud (jämna datum):"
        "FörbudJämnUdda" -> "Förbud (jämna/udda datum):"
        "FörbudParkera" -> "Förbud att parkera:"
        "FörbudStop" -> "Förbud att stanna:"
        "FörbudUdda" -> "Förbud (udda datum):"
        "Huvudled" -> "Huvudled"
        "HögerVänsterPil" -> "Pil åt båda håll"
        "Laddplats" -> "Laddplats"
        "P-skiva" -> "P-skiva krävs"
        "P-symbol" -> "P-skylt"
        "Pil höger" -> "Pil till höger"
        "Pil vänster" -> "Pil till vänster"
        "RakParkering" -> "Rak parkering"
        "Tider" -> "Parkerings-tider"
        else -> "Okänd skylt"
    }
}
