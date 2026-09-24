package com.riceleaf.local.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _modelName = MutableStateFlow(
        prefs.getString(KEY_MODEL, "rice_leaf_model.onnx") ?: "rice_leaf_model.onnx"
    )
    val modelName: StateFlow<String> = _modelName

    private val _customLeafPositions = MutableStateFlow(
        prefs.getStringSet(KEY_CUSTOM_LEAVES, emptySet())?.toList()?.sorted() ?: emptyList()
    )
    val customLeafPositions: StateFlow<List<String>> = _customLeafPositions

    fun init() {}

    fun setModelName(value: String) {
        _modelName.value = value
        prefs.edit().putString(KEY_MODEL, value).apply()
    }

    fun addCustomLeafPosition(value: String) {
        val updated = (_customLeafPositions.value + value).distinct().sorted()
        _customLeafPositions.value = updated
        prefs.edit().putStringSet(KEY_CUSTOM_LEAVES, updated.toSet()).apply()
    }

    fun removeCustomLeafPosition(value: String) {
        val updated = _customLeafPositions.value - value
        _customLeafPositions.value = updated
        prefs.edit().putStringSet(KEY_CUSTOM_LEAVES, updated.toSet()).apply()
    }

    companion object {
        private const val PREFS_NAME = "riceleaf_local_settings"
        private const val KEY_MODEL = "model_name"
        private const val KEY_CUSTOM_LEAVES = "custom_leaf_positions"
    }
}
