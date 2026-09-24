package com.riceleaf.local.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightGreen = Color(0xFF2E7D32)
private val LightGreenVariant = Color(0xFF60AD5E)

private val LightColorScheme = lightColorScheme(
    primary = LightGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    secondary = LightGreenVariant,
    surface = Color(0xFFFFFBFE),
    background = Color.White,
)

@Composable
fun RiceLeafTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
