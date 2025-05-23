package com.appcues.debugger.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light colors listed here can be found in
// https://www.figma.com/file/8Sozh9JnseLTF5oWjvGfIp/Foundations?type=design&node-id=1411-2104&mode=design&t=bwZeYcUjHziF2oX5-0

private val Neutral0 = Color(color = 0xFFFFFFFF)
private val Neutral50 = Color(color = 0xFFF7FAFF)
private val Neutral500 = Color(color = 0xFF8492AE)
private val Neutral600 = Color(color = 0xFF627293)
private val Neutral700 = Color(color = 0xFF425678)
private val Neutral800 = Color(color = 0xFF141923)
private val Blue600 = Color(color = 0xFF0072D6)
private val Pink600 = Color(color = 0xFFDD2270)
private val Blurple100 = Color(color = 0xFFEEEEFF)
private val Blurple600 = Color(color = 0xFF5C5CFF)
private val Blurple700 = Color(color = 0xFF4343C5)
private val Yellow600 = Color(color = 0xFFA66300)
private val Green600 = Color(color = 0xFF108484)

// Composing a color palette for light mode are based on foundation figma doc.
// Visit: https://www.figma.com/file/8Sozh9JnseLTF5oWjvGfIp/Foundations?type=design&node-id=5619-2635&mode=design&t=56CipGcX1bXwpUOW-0
internal fun appcuesLightColors(isTesting: Boolean) = AppcuesThemeColors(
    background = Neutral0,
    backgroundBranded = Blurple100,
    backgroundSelected = Neutral50,
    error = Pink600,
    warning = Yellow600,
    loading = if (isTesting) Color.Transparent else Blue600,
    info = Blue600,
    success = Green600,
    link = Blurple700,
    primary = Neutral800,
    secondary = Neutral700,
    tertiary = Neutral600,
    brand = Blurple600,
    input = Neutral500,
    inputActive = Blue600,
    gradientDismiss = Brush.radialGradient(listOf(Color(color = 0x30000000), Color.Transparent)),
    primaryButton = Brush.horizontalGradient(listOf(Color(color = 0xFF5C5CFF), Color(color = 0xFF7D52FF)))
)
