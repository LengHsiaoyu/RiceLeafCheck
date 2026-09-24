package com.riceleaf.app.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riceleaf.app.network.DetectResponse
import com.riceleaf.app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

data class CameraUiState(
    val samplePoint: String = "",
    val plantNo: String = "1",
    val leafPosition: String = "倒3叶",
    val shotCount: Int = 0,
    val isUploading: Boolean = false,
    val detectResult: DetectResponse? = null,
    val errorMessage: String? = null,
    val modelType: String = "local"
)

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState

    private val leafCycle = listOf("倒3叶", "倒2叶", "倒1叶")
    private var cycleIndex = 0

    init { loadTotalCount() }

    private fun loadTotalCount() {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.apiService.getRecordCount()
                if (resp.code == 200 && resp.data != null) {
                    _uiState.value = _uiState.value.copy(shotCount = resp.data.toInt())
                }
            } catch (_: Exception) {}
        }
    }

    fun setModelType(model: String) {
        _uiState.value = _uiState.value.copy(modelType = model)
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

    fun clearResult() {
        _uiState.value = _uiState.value.copy(detectResult = null, errorMessage = null)
    }

    fun onError(message: String) {
        _uiState.value = _uiState.value.copy(isUploading = false, errorMessage = message)
    }

    fun autoAdvance() {
        cycleIndex = (cycleIndex + 1) % leafCycle.size
        val nextLeaf = leafCycle[cycleIndex]
        val state = _uiState.value
        val nextPlantNo = if (nextLeaf == "倒3叶") {
            ((state.plantNo.toIntOrNull() ?: 1) + 1).toString()
        } else state.plantNo

        _uiState.value = state.copy(
            leafPosition = nextLeaf,
            plantNo = nextPlantNo,
            shotCount = state.shotCount,
            detectResult = null,
            errorMessage = null
        )
    }

    fun uploadAndDetect(bitmap: Bitmap) {
        val state = _uiState.value
        if (state.samplePoint.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入样点编号")
            return
        }
        if (state.plantNo.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入株号")
            return
        }
        _uiState.value = state.copy(isUploading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val bytes = ByteArrayOutputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
                    it.toByteArray()
                }
                val imagePart = MultipartBody.Part.createFormData(
                    "image", "leaf.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                val modelPart = state.modelType.toRequestBody("text/plain".toMediaTypeOrNull())
                val api = RetrofitClient.apiService
                val resp = api.detectDisease(
                    image = imagePart,
                    samplePoint = state.samplePoint.toRequestBody("text/plain".toMediaTypeOrNull()),
                    plantNo = state.plantNo.toRequestBody("text/plain".toMediaTypeOrNull()),
                    leafPosition = state.leafPosition.toRequestBody("text/plain".toMediaTypeOrNull()),
                    model = modelPart
                )
                if (resp.code == 200 && resp.data != null) {
                    _uiState.value = _uiState.value.copy(isUploading = false, detectResult = resp.data, shotCount = state.shotCount + 1)
                } else {
                    _uiState.value = _uiState.value.copy(isUploading = false, errorMessage = resp.message ?: "识别失败")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUploading = false, errorMessage = "网络错误: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        val leafPositionOptions = listOf("倒3叶", "倒2叶", "倒1叶")
    }
}
