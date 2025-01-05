package com.example.parking.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parking.data.PredictionProcessor
import com.example.parking.data.buildDateExplanation
import com.example.parking.data.buildTimeExplanation
import com.example.parking.data.combineDayPrefixWithNextTimeRange
import com.example.parking.data.extractHoursFromPText
import com.example.parking.data.getCustomDescription
import com.example.parking.data.mapTagNameToDisplayName
import com.example.parking.data.isDateRange
import com.example.parking.data.isTimeRange
import com.example.parking.data.models.TiderLineInfo

@Composable
fun ResultScreen(
    results: Map<String, Any>,
    onBack: () -> Unit
) {
    // Hämta ALLA predictions
    val matchedPredictions = results["MatchedPredictions"] as? List<PredictionProcessor.PredictionResult>
        ?: emptyList()

    // Sätt vilka taggar som ska visas i huvudlistan
    val mainListTagNames = setOf(
        "FörbudStop",
        "FörbudParkera",
        "FörbudJämnUdda",
        "FörbudJämn",
        "FörbudUdda",
        "Tider",
        "Laddplats",
        "P-skiva",
        "P-symbol",
        "Huvudled",
        "HögerVänsterPil",
        "Pil vänster",
        "Pil höger",
        "RakParkering"
    )
    val mainListPredictions = matchedPredictions.filter { it.tagName in mainListTagNames }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resultat:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Visa alla skyltar i huvudsidan
        mainListPredictions.forEach { prediction ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(Modifier.padding(8.dp)) {
                    when (prediction.tagName) {
                        "Tider",
                        "FörbudStop",
                        "FörbudParkera",
                        "FörbudJämnUdda",
                        "FörbudJämn",
                        "FörbudUdda" -> {
                            // Visa rubrik och text/tider för Tider och Förbud
                            Text(
                                text = mapTagNameToDisplayName(prediction.tagName),
                                fontSize = 18.sp
                            )
                            if (prediction.tiderLines.isNotEmpty()) {
                                val combinedTiderLines = combineDayPrefixWithNextTimeRange(prediction.tiderLines)
                                combinedTiderLines.forEach { lineInfo ->
                                    if (isDateRange(lineInfo.text)) {
                                        Text("Gäller ${buildDateExplanation(lineInfo)}", fontSize = 16.sp)
                                    } else {
                                        Text(buildTimeExplanation(lineInfo), fontSize = 16.sp)
                                    }
                                }
                            } else {
                                Text(prediction.text, fontSize = 16.sp)
                            }
                        }
                        "P-skiva" -> {
                            // Visa beskrivning och extrahera timmar för P-skiva
                            Text(
                                text = prediction.description,
                                fontSize = 16.sp
                            )
                            if (prediction.text.isNotEmpty()) {
                                val hoursText = extractHoursFromPText(prediction.text)
                                if (hoursText.isNotEmpty()) {
                                    Text("Du får stå i $hoursText", fontSize = 16.sp)
                                }
                            }
                        }
                        else -> {
                            // Visa endast beskrivning för andra skyltar
                            Text(
                                text = prediction.description,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tillbaka-knapp
        Button(onClick = onBack) {
            Text("Tillbaka")
        }
    }
}
