package com.riceleaf.local.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riceleaf.local.data.local.entity.RecordEntity
import com.riceleaf.local.data.repository.PhotoRepository
import com.riceleaf.local.ml.InferenceEngine
import com.riceleaf.local.ml.InferenceResult
import com.riceleaf.local.util.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class CameraState { IDLE, CAPTURED, INFERRING, RESULT, ERROR }

data class CameraUiState(
    val cameraState: CameraState = CameraState.IDLE,
    val plantNo: String = "",
    val leafPosition: String = "倒3叶",
    val inferenceResult: InferenceResult? = null,
    val errorMessage: String? = null,
    val leafPositionOptions: List<String> = listOf("倒3叶", "倒2叶", "倒1叶"),
    val shotCount: Int = 0
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val inferenceEngine: InferenceEngine,
    private val repository: PhotoRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap

    init {
        viewModelScope.launch {
            combine(
                settingsManager.customLeafPositions,
                repository.getRecordCount()
            ) { customs, count ->
                val defaults = listOf("倒3叶", "倒2叶", "倒1叶")
                val all = defaults + customs.filter { it !in defaults }
                _uiState.value.copy(
                    leafPositionOptions = all,
                    shotCount = count
                )
            }.collect { _uiState.value = it }
        }
    }

    fun updatePlantNo(value: String) {
        _uiState.value = _uiState.value.copy(plantNo = value)
    }

    fun updateLeafPosition(value: String) {
        _uiState.value = _uiState.value.copy(leafPosition = value)
    }

    fun addCustomLeafPosition(value: String) {
        if (value.isNotBlank()) settingsManager.addCustomLeafPosition(value)
    }

    fun onCapture(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
        _uiState.value = _uiState.value.copy(
            cameraState = CameraState.CAPTURED,
            errorMessage = null
        )
    }

    fun onDiscard() {
        _capturedBitmap.value = null
        _uiState.value = _uiState.value.copy(
            cameraState = CameraState.IDLE,
            errorMessage = null
        )
    }

    fun onDismissResult() {
        _capturedBitmap.value = null
        _uiState.value = _uiState.value.copy(
            cameraState = CameraState.IDLE,
            inferenceResult = null
        )
    }

    fun onConfirm() {
        val bitmap = _capturedBitmap.value ?: return
        val state = _uiState.value

        if (state.plantNo.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入株号")
            return
        }

        _uiState.value = state.copy(cameraState = CameraState.INFERRING, errorMessage = null)

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    inferenceEngine.run(bitmap)
                }

                val imagePath = withContext(Dispatchers.Default) {
                    repository.saveImage(bitmap)
                }

                val record = RecordEntity(
                    imagePath = imagePath,
                    plantNo = state.plantNo,
                    leafPosition = state.leafPosition,
                    diseaseName = result.className,
                    confidence = result.confidence,
                    lesionAreaRatio = result.lesionAreaRatio,
                    severityLevel = result.severityLevel,
                    symptomDesc = result.symptomDesc
                )
                repository.insertRecord(record)

                _uiState.value = _uiState.value.copy(
                    cameraState = CameraState.RESULT,
                    inferenceResult = result,
                    shotCount = _uiState.value.shotCount + 1
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cameraState = CameraState.ERROR,
                    errorMessage = e.message ?: "推理失败"
                )
            }
        }
    }
}
