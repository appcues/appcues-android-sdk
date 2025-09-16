package com.appcues.samples.kotlin.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5C5CFF),
    secondary = Color(0xFF2CB4FF),
    tertiary = Color(0xFFFF5290)
)

@Suppress("MagicNumber")
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5C5CFF),
    secondary = Color(0xFF2CB4FF),
    tertiary = Color(0xFFFF5290)
)

@Composable
fun AppcuesComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
