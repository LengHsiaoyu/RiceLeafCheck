package com.riceleaf.local.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riceleaf.local.ml.InferenceEngine
import com.riceleaf.local.ml.ModelInfo
import com.riceleaf.local.util.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentModelName: String = "",
    val installedModels: List<ModelInfo> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val inferenceEngine: InferenceEngine,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            settingsManager.modelName.collect { name ->
                val models = inferenceEngine.getInstalledModels()
                _uiState.value = SettingsUiState(
                    currentModelName = name,
                    installedModels = models
                )
            }
        }
    }

    fun selectModel(name: String) {
        settingsManager.setModelName(name)
    }

    fun refreshModels() {
        val models = inferenceEngine.getInstalledModels()
        _uiState.value = _uiState.value.copy(installedModels = models)
    }
}
