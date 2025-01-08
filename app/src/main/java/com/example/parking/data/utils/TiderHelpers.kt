package com.example.parking.data.utils

import com.example.parking.data.models.TiderLineInfo

fun isTimeRange(text: String): Boolean {
    val timeRangeRegex = Regex("""\d{1,2}\s*-\s*\d{1,2}""")
    return timeRangeRegex.containsMatchIn(text)
}

fun isDateRange(text: String): Boolean {
    return text.contains("/")
}


fun looksLikeDay(text: String): Boolean {
    val dayRegex = Regex("^(mån|månd|tis|tisd|ons|onsd|tors|torsd|fre|fred|lör|lörd|sön|sönd)", RegexOption.IGNORE_CASE)
    return dayRegex.find(text.trim()) != null
}


fun combineDayPrefixWithNextTimeRange(tiderLines: List<TiderLineInfo>): List<TiderLineInfo> {
    val combinedList = mutableListOf<TiderLineInfo>()
    var skipNext = false

    for (i in tiderLines.indices) {
        if (skipNext) {
            skipNext = false
            continue
        }

        val current = tiderLines[i]

        if (!isTimeRange(current.text) && looksLikeDay(current.text)) {
            if (i < tiderLines.size - 1) {
                val next = tiderLines[i + 1]
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
                    combinedList.add(current)
                }
            } else {
                combinedList.add(current)
            }
        } else {
            combinedList.add(current)
        }
    }
    return combinedList
}

fun buildTimeExplanation(lineInfo: TiderLineInfo): String {
    val rawText = lineInfo.text.trim()


    if (isDateRange(rawText)) {
        return buildDateExplanation(lineInfo)
    }

    if (!isTimeRange(rawText)) {
        return rawText
    }

    val isRed = lineInfo.isRed
    val isParenthesized = rawText.startsWith("(") && rawText.endsWith(")")
    val cleanLine = rawText.removeSurrounding("(", ")").trim()


    val dayRegex = Regex("^(Mån|Månd|Måndag|Tis|Tisd|Tisdag|Ons|Onsd|Onsdag|Tors|Torsd|Torsdag|Fre|Fred|Fredag|Lör|Lörd|Lördag|Sön|Sönd|Söndag)\\b", RegexOption.IGNORE_CASE)

    val matchResult = dayRegex.find(cleanLine)
    val foundDay = matchResult?.value.orEmpty()


    val rest = if (foundDay.isNotEmpty()) {
        cleanLine.substring(foundDay.length).trim()
    } else {
        cleanLine
    }



    val timeParts = rest.split("-").map { it.trim() }
    val timeString = if (timeParts.size == 2) {
        "${timeParts[0]} till ${timeParts[1]}"
    } else rest


    val dayString = when (foundDay.lowercase()) {
        "mån", "månd", "måndag" -> "Måndag"
        "tis", "tisd", "tisdag" -> "Tisdag"
        "ons", "onsd", "onsdag" -> "Onsdag"
        "tors", "torsd", "torsdag" -> "Torsdag"
        "fre", "fred", "fredag" -> "Fredag"
        "lör", "lörd", "lördag" -> "Lördag"
        "sön", "sönd", "söndag" -> "Söndag"
        else -> foundDay
    }


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
    val hoursRegex = Regex("""(\d+)\s*tim""")
    val match = hoursRegex.find(text)
    return match?.groupValues?.get(1)?.let { "$it timmar" } ?: ""
}
