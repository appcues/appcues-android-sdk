package com.appcues.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Official AppcuesTheme for all compositions
 */
@Composable
internal fun AppcuesExperienceTheme(
    content: @Composable () -> Unit
) {
    val colors = if (isSystemInDarkTheme()) darkAppcuesColors() else lightAppcuesColors()
    CompositionLocalProvider(value = LocalAppcuesColors provides colors) {
        content()
    }
}

/**
 * CompositionLocal used to pass [Colors] down the tree.
 *
 */
internal val LocalAppcuesColors = staticCompositionLocalOf { lightAppcuesColors() }

internal data class AppcuesColors(
    val background: Color,
    val onBackground: Color,
    val primary: Color,
    val secondary: Color,
)

private fun lightAppcuesColors(
    background: Color = Color.White,
    text: Color = Color.Black,
    primary: Color = Color(0xFF5C5CFF),
    secondary: Color = Color(0xFF2CB4FF),
): AppcuesColors = AppcuesColors(
    background = background,
    onBackground = text,
    primary = primary,
    secondary = secondary,
)

private fun darkAppcuesColors(
    background: Color = Color.Black,
    text: Color = Color.White,
    primary: Color = Color(0xFF5C5CFF),
    secondary: Color = Color(0xFF2CB4FF),
): AppcuesColors = AppcuesColors(
    background = background,
    onBackground = text,
    primary = primary,
    secondary = secondary,
)
