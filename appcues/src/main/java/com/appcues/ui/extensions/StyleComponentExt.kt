package com.appcues.ui.extensions

import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import java.io.File

internal fun ComponentStyle.getMargins() = PaddingValues(
    start = marginLeading.dp,
    top = marginTop.dp,
    bottom = marginBottom.dp,
    end = marginTrailing.dp,
)

internal fun ComponentStyle.getPaddings() = PaddingValues(
    start = paddingLeading.dp,
    bottom = paddingBottom.dp,
    top = paddingTop.dp,
    end = paddingTrailing.dp,
)

internal fun ComponentStyle.getTextAlignment(): TextAlign? {
    return when (textAlignment) {
        ComponentHorizontalAlignment.LEADING -> TextAlign.Start
        ComponentHorizontalAlignment.CENTER -> TextAlign.Center
        ComponentHorizontalAlignment.TRAILING -> TextAlign.End
        null -> null
    }
}

internal fun ComponentStyle.getFontFamily(context: Context): FontFamily? {
    return FontFamily.getSystemFontFamily(fontName)
        ?: FontFamily.getFontResource(context, fontName)
        ?: FontFamily.getFontAsset(context, fontName)
        ?: FontFamily.getSystemFont(fontName)
}

// handle system fonts available in Compose with variable weight
// convention is name of: "System {FamilyName} {Weight}"
private fun FontFamily.Companion.getSystemFontFamily(fontName: String?): FontFamily? {
    if (fontName != null && fontName.lowercase().startsWith("system ")) {
        val tokens = fontName.split(" ")
        if (tokens.count() > 1) {
            val systemFontFamily = tokens[1]
            return FontFamily.get(systemFontFamily)
        }
    }
    return null
}

// try to load a custom font from a resource in the host application
private fun FontFamily.Companion.getFontResource(context: Context, fontName: String?): FontFamily? {
    if (fontName != null) {
        // this is an attempt to provide convenience to convert to valid resource names, for ex. if a font
        // name of "Lato-Black" was used, it will auto convert to "lato_black" resource name
        val resourceName = fontName.lowercase().replace("-", "_")
        val fontId = context.resources.getIdentifier(resourceName, "font", context.packageName)
        if (fontId != 0) {
            return FontFamily(Font(fontId))
        }
    }
    return null
}

// try to load a custom font from an embedded asset in the host application
private fun FontFamily.Companion.getFontAsset(context: Context, fontName: String?): FontFamily? {
    if (fontName != null) {
        val assetName = "$fontName.ttf"
        val fontsInAssets = context.assets.list("fonts")
        if (fontsInAssets != null && fontsInAssets.contains(assetName)) {
            val typeface = Typeface.createFromAsset(context.assets, "fonts/$fontName.ttf")
            if (typeface != null) {
                return FontFamily(typeface)
            }
        }
    }
    return null
}

// try to load an individual font file from the Android system path
private fun FontFamily.Companion.getSystemFont(fontName: String?): FontFamily? {
    if (fontName != null) {
        val file = File("/system/fonts/$fontName.ttf")
        if (file.exists()) {
            val typeface = Typeface.createFromFile(file)
            if (typeface != null) {
                return FontFamily(typeface)
            }
        }
    }
    return null
}

internal fun FontFamily.Companion.get(name: String): FontFamily {
    return when (name.lowercase()) {
        "serif" -> Serif
        "sansserif" -> SansSerif
        "monospaced" -> Monospace
        "cursive" -> Cursive
        else -> Default
    }
}

internal fun FontWeight.Companion.get(name: String): FontWeight {
    // NOTE: naming conventions here match iOS, rather than Android system defaults
    // for ease of use in the Appcues builder across platforms
    return when (name.lowercase()) {
        // 100
        "ultralight" -> Thin
        // 200
        "thin" -> ExtraLight
        // 300
        "light" -> Light
        // 500
        "medium" -> Medium
        // 600
        "semibold" -> SemiBold
        // 700
        "bold" -> Bold
        // 800
        "heavy" -> ExtraBold
        // 900
        "black" -> Black
        // 400
        else -> Normal
    }
}

internal fun ComponentStyle.getFontWeight(): FontWeight? {
    // font weight only supported on system fonts with naming convention
    // convention is name of: "System {FamilyName} {Weight}"
    if (fontName != null) {
        if (fontName.lowercase().startsWith("system ")) {
            val tokens = fontName.split(" ")
            if (tokens.count() > 2) {
                val systemFontWeight = tokens[2]
                return FontWeight.get(systemFontWeight)
            }
        }
    }
    return null
}

internal fun ComponentStyle.getVerticalAlignment(): Alignment.Vertical {
    return when (verticalAlignment) {
        ComponentVerticalAlignment.TOP -> Alignment.Top
        ComponentVerticalAlignment.CENTER -> Alignment.CenterVertically
        ComponentVerticalAlignment.BOTTOM -> Alignment.Bottom
        null -> Alignment.CenterVertically
    }
}

internal fun ComponentStyle.getHorizontalAlignment(): Alignment.Horizontal {
    return when (horizontalAlignment) {
        ComponentHorizontalAlignment.LEADING -> Alignment.Start
        ComponentHorizontalAlignment.CENTER -> Alignment.CenterHorizontally
        ComponentHorizontalAlignment.TRAILING -> Alignment.End
        null -> Alignment.CenterHorizontally
    }
}

internal fun ComponentStyle?.getBoxAlignment(): Alignment {
    if (this == null) return Alignment.Center

    val horizontalBias = when (horizontalAlignment) {
        ComponentHorizontalAlignment.LEADING -> -1f
        ComponentHorizontalAlignment.CENTER -> 0f
        ComponentHorizontalAlignment.TRAILING -> 1f
        null -> 0f
    }
    val verticalBias = when (verticalAlignment) {
        ComponentVerticalAlignment.TOP -> -1f
        ComponentVerticalAlignment.CENTER -> 0f
        ComponentVerticalAlignment.BOTTOM -> 1f
        null -> 0f
    }

    return BiasAlignment(horizontalBias, verticalBias)
}

internal fun ComponentStyle.getTextStyle(context: Context, isDark: Boolean): TextStyle {
    return TextStyle(
        color = foregroundColor.getColor(isDark) ?: Color.Unspecified,
        fontSize = fontSize?.sp ?: TextUnit.Unspecified,
        lineHeight = lineHeight?.sp ?: TextUnit.Unspecified,
        textAlign = getTextAlignment(),
        fontFamily = getFontFamily(context),
        letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
        fontWeight = getFontWeight(),
    )
}
