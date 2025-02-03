package com.appcues.debugger.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * AppcuesThemeColors defines all different identified properties that are relevant to our theme,
 *
 * Its always possible to add new properties here that represent different pieces of our branding
 */
@SuppressWarnings("LongParameterList")
internal class AppcuesThemeColors(
    background: Color,
    backgroundBranded: Color,
    backgroundSelected: Color,
    error: Color,
    warning: Color,
    loading: Color,
    info: Color,
    success: Color,
    link: Color,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    brand: Color,
    input: Color,
    inputActive: Color,
    gradientDismiss: Brush,
    primaryButton: Brush,
) {

    var background by mutableStateOf(background, structuralEqualityPolicy())
        internal set
    var backgroundBranded by mutableStateOf(backgroundBranded, structuralEqualityPolicy())
        internal set
    var backgroundSelected by mutableStateOf(backgroundSelected, structuralEqualityPolicy())
        internal set
    var error by mutableStateOf(error, structuralEqualityPolicy())
        internal set
    var warning by mutableStateOf(warning, structuralEqualityPolicy())
        internal set
    var info by mutableStateOf(info, structuralEqualityPolicy())
        internal set
    var loading by mutableStateOf(loading, structuralEqualityPolicy())
        internal set
    var success by mutableStateOf(success, structuralEqualityPolicy())
        internal set
    var link by mutableStateOf(link, structuralEqualityPolicy())
        internal set
    var primary by mutableStateOf(primary, structuralEqualityPolicy())
        internal set
    var secondary by mutableStateOf(secondary, structuralEqualityPolicy())
        internal set
    var tertiary by mutableStateOf(tertiary, structuralEqualityPolicy())
        internal set
    var brand by mutableStateOf(brand, structuralEqualityPolicy())
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
