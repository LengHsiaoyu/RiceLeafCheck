package com.riceleaf.local.ml

data class InferenceResult(
    val className: String,
    val confidence: Float,
    val top3: List<Prediction> = emptyList(),
    val lesionAreaRatio: Float = 0f,
    val severityLevel: Int = 0,
    val symptomDesc: String = ""
)

data class Prediction(
    val className: String,
    val confidence: Float
)
