package com.doceolab.milan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MilanColorScheme = lightColorScheme(
    primary = MilanBlue,
    secondary = MilanCyan,
    background = MilanSurface,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = MilanInk,
    onSurface = MilanInk,
)

@Composable
fun MilanTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MilanColorScheme, content = content)
}
