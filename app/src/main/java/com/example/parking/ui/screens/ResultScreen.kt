package com.example.parking.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parking.data.PredictionProcessor
import com.example.parking.data.customvision.Prediction

@Composable
fun ResultScreen(
    results: Map<String, Any>,
    onBack: () -> Unit
) {
    val matchedPredictions = results["MatchedPredictions"] as? List<PredictionProcessor.PredictionResult>

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resultat:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        matchedPredictions?.forEach { prediction ->
            Text(
                text = """
                Skylt: ${prediction.tagName}
                Text: ${prediction.text}
                Sannolikhet: ${(prediction.probability * 100).toInt()}%
                Beskrivning: ${prediction.description}
                """.trimIndent(),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("Tillbaka")
        }
    }
}
