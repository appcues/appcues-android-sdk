package com.appcues.debugger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr

/**
 * This is official Appcues Theme that applies all appcues branding colors
 * Not used by Experiences since we don't want to impose our colors in any experience,
 *
 * ONLY USE THIS FOR APPCUES SPECIFIC COMPOSITIONS LIKE OUR FAB(Floating Action Button)
 */
@Composable
internal fun AppcuesTheme(
    isTesting: Boolean,
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val appcuesTheme = if (isDark) appcuesDarkColors(isTesting) else appcuesLightColors(isTesting)

    MaterialTheme(colors = appcuesTheme.toMaterialTheme(isDark)) {
        CompositionLocalProvider(
            // Debugger is in English and always LTR regardless of app settings
            LocalLayoutDirection provides Ltr,
            LocalAppcuesTheme provides appcuesTheme
        ) {
            content()
        }
    }
}

internal val LocalAppcuesTheme = staticCompositionLocalOf { appcuesLightColors(false) }

/**
 * Based on our current theme, override some properties for the material theme
 * this is useful so that some Material components can pick up our themes as well
 */
private fun AppcuesThemeColors.toMaterialTheme(isDark: Boolean): Colors {
    val colors = if (isDark) darkColors() else lightColors()

    return colors.copy(
        surface = background,
        onSurface = primary,
        background = background,
        onBackground = primary,
        primary = brand
    )
}
