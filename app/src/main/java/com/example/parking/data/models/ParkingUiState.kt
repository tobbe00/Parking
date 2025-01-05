package com.example.parking.data.models

import com.example.parking.data.customvision.Prediction

data class ParkingUiState(
    val result: Map<String, Any> = emptyMap(),
    val errorMessage: String? = null,
    val highConfidencePredictions: List<Prediction> = emptyList(),
    val isLoading: Boolean = false
)