package com.appcues.debugger.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark colors listed here can be found in
// https://www.figma.com/file/8Sozh9JnseLTF5oWjvGfIp/Foundations?type=design&node-id=1411-2104&mode=design&t=bwZeYcUjHziF2oX5-0

private val Neutral0 = Color(color = 0xFFFFFFFF)
private val Neutral50 = Color(color = 0xFFE7EBF2)
private val Neutral300 = Color(color = 0xFF7186AE)
private val Neutral400 = Color(color = 0xFF99A6BF)
private val Neutral600 = Color(color = 0xFF222B3A)
private val Neutral700 = Color(color = 0xFF1E2635)
private val Blue300 = Color(color = 0xFF81B2EF)
private val Pink400 = Color(color = 0xFFE86891)
private val Blurple300 = Color(color = 0xFFA7A7F1)
private val Blurple400 = Color(color = 0xFF8787E8)
private val Yellow300 = Color(color = 0xFFD5A84D)

// Composing a color palette for dark mode are based on foundation figma doc.
// Visit: https://www.figma.com/file/8Sozh9JnseLTF5oWjvGfIp/Foundations?type=design&node-id=5619-2635&mode=design&t=56CipGcX1bXwpUOW-0
internal fun darkAppcuesTheme() = AppcuesThemeColors(
    background = Neutral600,
    backgroundSelected = Neutral700,
    error = Pink400,
    warning = Yellow300,
    info = Blue300,
    link = Blurple300,
    primary = Neutral0,
    secondary = Neutral50,
    brand = Blurple400,
    divider = Neutral400,
    input = Neutral300,
    inputActive = Blue300,
    gradientDismiss = Brush.radialGradient(listOf(Color(color = 0x30FFFFFF), Color.Transparent)),
    primaryButton = Brush.horizontalGradient(listOf(Color(color = 0xFF4F4FC4), Color(color = 0xFF8250B4)))
)
