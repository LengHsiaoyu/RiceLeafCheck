package com.riceleaf.local.ml

import com.google.gson.Gson

import java.io.InputStream
import java.io.InputStreamReader

data class ModelInfo(
    val name: String = "",
    val version: String = "1.0.0",
    val format: String = "onnx",
    val inputSize: List<Int> = listOf(224, 224),
    val normalize: NormalizeParams = NormalizeParams(),
    val classes: List<String> = emptyList(),
    val severityThresholds: List<List<Float>> = listOf(
        listOf(0f, 0f), listOf(1f, 5f), listOf(3f, 10f), listOf(5f, 25f), listOf(7f, 50f), listOf(9f, 100f)
    ),
    val symptomDesc: Map<String, String> = emptyMap()
) {
    data class NormalizeParams(
        val mean: List<Float> = listOf(0.485f, 0.456f, 0.406f),
        val std: List<Float> = listOf(0.229f, 0.224f, 0.225f)
    )

    companion object {
        fun fromJson(stream: InputStream): ModelInfo {
            return Gson().fromJson(InputStreamReader(stream), ModelInfo::class.java)
        }
    }
}
