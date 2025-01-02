package com.example.parking.data.models


data class ReadOperationResult(
    val status: String,
    val analyzeResult: AnalyzeResult?
)

data class AnalyzeResult(
    val readResults: List<ReadResult>?
)

data class ReadResult(
    val page: Int,
    val angle: Double,
    val width: Int,
    val height: Int,
    val unit: String,
    val lines: List<TextLine>?
)

data class TextLine(
    val boundingBox: List<Float>,
    val text: String,
    val words: List<TextWord>?
)

data class TextWord(
    val boundingBox: List<Float>,
    val text: String
)