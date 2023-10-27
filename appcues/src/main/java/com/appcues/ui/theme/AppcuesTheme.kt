package com.appcues.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Official AppcuesTheme for all compositions
 */
@Composable
internal fun AppcuesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = getColors(isSystemInDarkTheme()),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

private fun getColors(isDark: Boolean): Colors {

    return if (isDark)
        darkColors().copy(
            background = Color.Transparent,
            onBackground = Color.Transparent,
            surface = Color.Transparent
        )
    else
        lightColors().copy(
            background = Color.Transparent,
            onBackground = Color.Transparent,
            surface = Color.Transparent
        )
}
