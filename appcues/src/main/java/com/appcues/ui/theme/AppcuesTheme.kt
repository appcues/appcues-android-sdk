package com.appcues.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

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
    return if (isDark) darkColors() else lightColors()
}

/**
 * This is supposed to be used during inspection mode (Preview) only.
 */
@Composable
internal fun AppcuesPreview(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    LocalConfiguration.current.uiMode = if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO

    AppcuesTheme {
        Surface {
            content()
        }
    }
}
