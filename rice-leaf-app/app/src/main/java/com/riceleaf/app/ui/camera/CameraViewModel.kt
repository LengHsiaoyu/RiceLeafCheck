package com.riceleaf.app.ui.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riceleaf.app.data.repository.RecordRepository
import com.riceleaf.app.domain.model.DetectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CameraUiState(
    val capturedImageUri: Uri? = null,
    val samplePoint: String = "",
    val plantNo: String = "1",
    val leafPosition: String = "倒3叶",
    val isUploading: Boolean = false,
    val result: DetectionResult? = null,
    val error: String? = null,
    val shotCount: Int = 0
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: RecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            capturedImageUri = uri,
            result = null,
            error = null
        )
    }

    fun updateSamplePoint(value: String) {
        _uiState.value = _uiState.value.copy(samplePoint = value)
    }

    fun updatePlantNo(value: String) {
        _uiState.value = _uiState.value.copy(plantNo = value)
    }

    fun updateLeafPosition(value: String) {
        _uiState.value = _uiState.value.copy(leafPosition = value)
    }

    fun upload() {
        val state = _uiState.value
        val uri = state.capturedImageUri ?: return

        _uiState.value = state.copy(isUploading = true, error = null)

        viewModelScope.launch {
            repository.detect(uri, state.samplePoint, state.plantNo, state.leafPosition)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        result = result,
                        shotCount = _uiState.value.shotCount + 1
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        error = e.message ?: "上传失败"
                    )
                }
        }
    }

    fun continueShooting() {
        val current = _uiState.value
        val nextLeaf = when (current.leafPosition) {
            "倒3叶" -> "倒2叶"
            "倒2叶" -> "倒1叶"
            "倒1叶" -> {
                val nextPlant = (current.plantNo.toIntOrNull() ?: 0) + 1
                _uiState.value = _uiState.value.copy(plantNo = nextPlant.toString())
                "倒3叶"
            }
            else -> "倒3叶"
        }
        _uiState.value = _uiState.value.copy(
            capturedImageUri = null,
            result = null,
            error = null,
            leafPosition = nextLeaf
        )
    }

    fun retake() {
        _uiState.value = _uiState.value.copy(
            capturedImageUri = null,
            result = null,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
