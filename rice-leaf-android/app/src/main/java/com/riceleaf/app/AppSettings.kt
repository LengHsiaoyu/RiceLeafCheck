package com.riceleaf.app

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppSettings {
    private const val PREFS_NAME = "riceleaf_settings"
    private const val KEY_MODEL = "model_type"

    private val _modelType = MutableStateFlow("local")
    val modelType: StateFlow<String> = _modelType

    private var ctx: Context? = null

    fun init(context: Context) {
        ctx = context
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _modelType.value = prefs.getString(KEY_MODEL, "local") ?: "local"
    }

    fun setModelType(value: String) {
        _modelType.value = value
        ctx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putString(KEY_MODEL, value)?.apply()
    }
}
