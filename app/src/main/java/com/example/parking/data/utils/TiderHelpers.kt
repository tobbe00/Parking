package com.example.parking.data.utils

import com.example.parking.data.models.TiderLineInfo

/**
 * Exempel: kollar om texten innehåller något som ser ut som ett tidsintervall (t.ex. "8-16").
 * Anpassa efter hur ditt OCR result brukar se ut.
 */
fun isTimeRange(text: String): Boolean {
    val timeRangeRegex = Regex("""\d{1,2}\s*-\s*\d{1,2}""")  // "8-16", "0-6" etc.
    return timeRangeRegex.containsMatchIn(text)
}

/**
 * Kollar om texten innehåller ett datumintervall, ex "1/11 - 15/5".
 */
fun isDateRange(text: String): Boolean {
    // En enkel check: om texten innehåller "1/11 - 15/5" eller liknande
    // t.ex. "1/11 - 30/3", "15/5 - 15/9", etc.
    // Du kan göra mer avancerad regex om du vill men vi kollar bara om "/" finns.
    return text.contains("/")
}

/**
 * Kollar om texten (t.ex. "Månd", "Tisd", "Ons") ser ut som en veckodag.
 */
fun looksLikeDay(text: String): Boolean {
    val dayRegex = Regex("^(mån|månd|tis|tisd|ons|onsd|tors|torsd|fre|fred|lör|lörd|sön|sönd)", RegexOption.IGNORE_CASE)
    return dayRegex.find(text.trim()) != null
}

/**
 * Kombinerar t.ex. (Tisd, 8-16) till "Tisd 8-16" om de ligger intill varandra i listan.
 * Returnerar en ny lista med hopslagna TiderLineInfo (om möjligt).
 */
fun combineDayPrefixWithNextTimeRange(tiderLines: List<TiderLineInfo>): List<TiderLineInfo> {
    val combinedList = mutableListOf<TiderLineInfo>()
    var skipNext = false

    for (i in tiderLines.indices) {
        if (skipNext) {
            skipNext = false
            continue
        }

        val current = tiderLines[i]

        // Om "current" är dagprefix (ej time range) OCH nästa är time range => slå ihop
        if (!isTimeRange(current.text) && looksLikeDay(current.text)) {
            // Finns nästa TiderLineInfo?
            if (i < tiderLines.size - 1) {
                val next = tiderLines[i + 1]
                // Är nästa en time range (typ "8-16")?
                if (isTimeRange(next.text)) {
                    val mergedText = "${current.text} ${next.text}".trim()
                    val mergedIsRed = current.isRed || next.isRed
                    combinedList.add(
                        TiderLineInfo(
                            text = mergedText,
                            isRed = mergedIsRed
                        )
                    )
                    skipNext = true
                } else {
                    // Går ej slå ihop, lägg till current som den är
                    combinedList.add(current)
                }
            } else {
                // ingen "nästa" => inget att slå ihop
                combinedList.add(current)
            }
        } else {
            // Normal fallback, lägg bara till current
            combinedList.add(current)
        }
    }
    return combinedList
}

/**
 * Om texten är t.ex. "Tisd 0-16" eller bara "0-16" -> bygg "Tisdag: 0 till 16" eller "Vardagar: 0 till 16".
 * Om texten visar "(0-6)" -> "Dag före röd dag: 0 till 6".
 * Om isRed -> "Röd dag: ..."
 * Om texten EJ är time range -> returnera text rakt av.
 */
fun buildTimeExplanation(lineInfo: TiderLineInfo): String {
    val rawText = lineInfo.text.trim()

    // 1) Är det datumintervall? (t.ex. "1/11 - 15/5")
    if (isDateRange(rawText)) {
        return buildDateExplanation(lineInfo)
    }

    // 2) Är det tidsintervall?
    if (!isTimeRange(rawText)) {
        return rawText
    }

    val isRed = lineInfo.isRed
    val isParenthesized = rawText.startsWith("(") && rawText.endsWith(")")
    val cleanLine = rawText.removeSurrounding("(", ")").trim()

    // 3) Identifiera dagprefix
    val dayRegex = Regex("^(Mån|Månd|Måndag|Tis|Tisd|Tisdag|Ons|Onsd|Onsdag|Tors|Torsd|Torsdag|Fre|Fred|Fredag|Lör|Lörd|Lördag|Sön|Sönd|Söndag)\\b", RegexOption.IGNORE_CASE)

    val matchResult = dayRegex.find(cleanLine)
    val foundDay = matchResult?.value.orEmpty()    // t.ex. "Tisd"

    // 4) Ta bort precis hela foundDay med substring
    val rest = if (foundDay.isNotEmpty()) {
        cleanLine.substring(foundDay.length).trim()
    } else {
        cleanLine
    }
    // t.ex. "Tisd 0-16" -> substring(4) -> "0-16"

    // 5) Bygg "8 till 16" etc.
    val timeParts = rest.split("-").map { it.trim() }
    val timeString = if (timeParts.size == 2) {
        "${timeParts[0]} till ${timeParts[1]}"
    } else rest

    // 6) Översätt dagprefix (”Tisd” → ”Tisdag” osv.)
    val dayString = when (foundDay.lowercase()) {
        "mån", "månd", "måndag" -> "Måndag"
        "tis", "tisd", "tisdag" -> "Tisdag"
        "ons", "onsd", "onsdag" -> "Onsdag"
        "tors", "torsd", "torsdag" -> "Torsdag"
        "fre", "fred", "fredag" -> "Fredag"
        "lör", "lörd", "lördag" -> "Lördag"
        "sön", "sönd", "söndag" -> "Söndag"
        else -> foundDay // fallback
    }

    // 7) Returnera slutsträng beroende på isRed/isParenthesized/dagprefix
    return when {
        isRed -> {
            if (dayString.isNotEmpty()) "Röd dag: $dayString $timeString"
            else "Röd dag: $timeString"
        }
        isParenthesized -> {
            if (dayString.isNotEmpty()) "Dag före röd dag: $dayString $timeString"
            else "Dag före röd dag: $timeString"
        }
        dayString.isNotEmpty() -> {
            "$dayString: $timeString"
        }
        else -> {
            "Vardagar: $timeString"
        }
    }
}


fun buildDateExplanation(lineInfo: TiderLineInfo): String {
    val text = lineInfo.text.trim()
    val parts = text.split("-").map { it.trim() }
    return if (parts.size == 2) {
        "${parts[0]} till ${parts[1]}"
    } else text
}


fun extractHoursFromPText(text: String): String {
    val hoursRegex = Regex("""(\d+)\s*tim""") // Matchar t.ex. "9 tim"
    val match = hoursRegex.find(text)
    return match?.groupValues?.get(1)?.let { "$it timmar" } ?: ""
}
