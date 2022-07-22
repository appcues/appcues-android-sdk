package com.appcues.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.ui.primitive.Compose

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

/**
 * This is supposed to be used during inspection mode (Preview) only.
 */
@Composable
internal fun AppcuesPreviewPrimitive(
    isDark: Boolean = isSystemInDarkTheme(),
    primitiveBuilder: () -> ExperiencePrimitive
) {
    LocalConfiguration.current.uiMode = if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO

    AppcuesTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colors.surface),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            primitiveBuilder().Compose()
        }
    }
}
