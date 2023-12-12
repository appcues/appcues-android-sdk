package com.appcues.debugger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr

/**
 * AppcuesThemeColors defines all different identified properties that are relevant to our theme,
 *
 * Its always possible to add new properties here that represent different pieces of our branding
 */
@SuppressWarnings("LongParameterList")
internal class AppcuesThemeColors(
    background: Color,
    backgroundSelected: Color,
    error: Color,
    warning: Color,
    info: Color,
    link: Color,
    primary: Color,
    secondary: Color,
    brand: Color,
    divider: Color,
    input: Color,
    inputActive: Color,
    gradientDismiss: Brush,
    primaryButton: Brush,
) {

    var background by mutableStateOf(background, structuralEqualityPolicy())
        internal set
    var backgroundSelected by mutableStateOf(backgroundSelected, structuralEqualityPolicy())
        internal set
    var error by mutableStateOf(error, structuralEqualityPolicy())
        internal set
    var warning by mutableStateOf(warning, structuralEqualityPolicy())
        internal set
    var info by mutableStateOf(info, structuralEqualityPolicy())
        internal set
    var link by mutableStateOf(link, structuralEqualityPolicy())
        internal set
    var primary by mutableStateOf(primary, structuralEqualityPolicy())
        internal set
    var secondary by mutableStateOf(secondary, structuralEqualityPolicy())
        internal set
    var brand by mutableStateOf(brand, structuralEqualityPolicy())
        internal set
    var divider by mutableStateOf(divider, structuralEqualityPolicy())
        internal set
    var input by mutableStateOf(input, structuralEqualityPolicy())
        internal set
    var inputActive by mutableStateOf(inputActive, structuralEqualityPolicy())
        internal set
    var gradientDismiss by mutableStateOf(gradientDismiss, structuralEqualityPolicy())
        internal set
    var primaryButton by mutableStateOf(primaryButton, structuralEqualityPolicy())
        internal set
}

internal val LocalAppcuesTheme = staticCompositionLocalOf { lightAppcuesTheme() }

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

/**
 * This is official Appcues Theme that applies all appcues branding colors
 * Not used by Experiences since we don't want to impose our colors in any experience,
 *
 * ONLY USE THIS FOR APPCUES SPECIFIC COMPOSITIONS LIKE OUR FAB(Floating Action Button)
 */
@Composable
internal fun AppcuesTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val appcuesTheme = if (isDark) darkAppcuesTheme() else lightAppcuesTheme()

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
