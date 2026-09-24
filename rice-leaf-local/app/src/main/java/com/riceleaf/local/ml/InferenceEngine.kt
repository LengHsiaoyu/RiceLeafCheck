package com.riceleaf.local.ml

import ai.onnxruntime.*
import android.content.Context
import android.graphics.Bitmap
import com.riceleaf.local.util.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) {
    private val env = OrtEnvironment.getEnvironment()
    private var currentSession: OrtSession? = null
    private var currentInfo: ModelInfo? = null
    private var currentModelName: String? = null

    private fun getModelsDir(): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun ensureModelAvailable() {
        val modelName = settingsManager.modelName.value
        val modelDir = getModelsDir()
        val modelFile = File(modelDir, modelName)

        if (!modelFile.exists()) {
            try {
                context.assets.open("models/$modelName").use { input ->
                    FileOutputStream(modelFile).use { output -> input.copyTo(output) }
                }
            } catch (_: Exception) {}
        }

        // Copy external data file if present (large models use external weights)
        val dataFileName = "$modelName.data"
        val dataFile = File(modelDir, dataFileName)
        if (!dataFile.exists()) {
            try {
                context.assets.open("models/$dataFileName").use { input ->
                    FileOutputStream(dataFile).use { output -> input.copyTo(output) }
                }
            } catch (_: Exception) {}
        }

        val infoFile = File(modelDir, modelName.replace(".onnx", "_info.json"))
        if (!infoFile.exists()) {
            try {
                context.assets.open("models/model_info.json").use { input ->
                    FileOutputStream(infoFile).use { output -> input.copyTo(output) }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadModel(modelName: String) {
        if (modelName == currentModelName && currentSession != null) return

        currentSession?.close()
        currentSession = null
        currentInfo = null

        val modelFile = File(getModelsDir(), modelName)
        if (!modelFile.exists()) return

        val infoFile = File(getModelsDir(), modelName.replace(".onnx", "_info.json"))
        if (!infoFile.exists()) return

        try {
            currentSession = env.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
            currentInfo = ModelInfo.fromJson(infoFile.inputStream())
            currentModelName = modelName
        } catch (e: Exception) {
            currentSession = null
            currentInfo = null
        }
    }

    suspend fun run(bitmap: Bitmap): InferenceResult = withContext(Dispatchers.Default) {
        // Resize large bitmaps to reduce memory pressure
        val input = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val maxDim = maxOf(bitmap.width, bitmap.height)
            val scale = 1024f / maxDim
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else bitmap

        try {
            val modelName = settingsManager.modelName.value
            ensureModelAvailable()
            loadModel(modelName)

            val session = currentSession ?: throw IllegalStateException(
                "模型未加载: $modelName, 请确保 assets/models/ 中有对应的 .onnx 文件"
            )
            val info = currentInfo ?: throw IllegalStateException("模型信息未加载")

            val tensor = Preprocessor.preprocess(input, info, env)
            val outputs = session.run(mapOf("input" to tensor))
            val rawOutput = outputs.get(0)

            @Suppress("UNCHECKED_CAST")
            val floats = (rawOutput.value as Array<FloatArray>).flatMap { it.toList() }

            tensor.close()
            outputs.close()

            val maxLogit = floats.maxOrNull() ?: 0f
            val expFloats = floats.map { kotlin.math.exp((it - maxLogit).toDouble()) }
            val sumExp = expFloats.sum()
            val probs = expFloats.map { (it / sumExp).toFloat() }

            val sortedIndices = probs.indices.sortedByDescending { probs[it] }
            val top1Idx = sortedIndices[0]
            val className = info.classes.getOrElse(top1Idx) { "未知" }
            val confidence = probs[top1Idx]

            val top3 = sortedIndices.take(3).map { idx ->
                Prediction(info.classes.getOrElse(idx) { "未知" }, probs[idx])
            }

            val isHealthy = className == "健康"
            val lesionRatio = if (isHealthy) 0f else LesionEstimator.estimateLesionRatio(input)
            val severity = if (isHealthy) 0 else SeverityMapper.mapLesionRatioToSeverity(
                lesionRatio, info.severityThresholds
            )
            val symptom = info.symptomDesc[className] ?: ""

            InferenceResult(className, confidence, top3, lesionRatio, severity, symptom)
        } finally {
            if (input !== bitmap) input.recycle()
        }
    }

    fun getModelInfo(): ModelInfo? = currentInfo

    fun getInstalledModels(): List<ModelInfo> {
        val modelsDir = getModelsDir()
        return modelsDir.listFiles()
            ?.filter { it.extension == "onnx" }
            ?.mapNotNull { modelFile ->
                val infoFile = File(getModelsDir(), modelFile.name.replace(".onnx", "_info.json"))
                if (infoFile.exists()) {
                    try { ModelInfo.fromJson(infoFile.inputStream()) }
                    catch (_: Exception) { null }
                } else null
            } ?: emptyList()
    }
}
