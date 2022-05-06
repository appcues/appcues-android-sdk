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
    if (fontName != null) {
        if (fontName.lowercase().startsWith("system ")) {
            // handle system fonts
            // convention is name of: "System {FamilyName} {Weight}"
            val tokens = fontName.split(" ")
            if (tokens.count() > 1) {
                val systemFontFamily = tokens[1]
                return FontFamily.get(systemFontFamily)
            }
        } else {
            // custom fonts

            // it might be a font in resources
            val fontId = context.resources.getIdentifier(fontName, "font", context.packageName)
            if (fontId != 0) {
                return FontFamily(Font(fontId))
            }

            // or it might be a font from assets
            val assetName = "${fontName}.ttf"
            val fontsInAssets = context.assets.list("fonts")
            if (fontsInAssets != null && fontsInAssets.contains(assetName)) {
                val typeface = Typeface.createFromAsset(context.assets, "fonts/${fontName}.ttf")
                if (typeface != null) {
                    return FontFamily(typeface)
                }
            }
        }
    }
    return null
}

internal fun FontFamily.Companion.get(name: String): FontFamily {
    return when (name.lowercase()) {
        "serif" -> Serif
        "sansserif" -> SansSerif
        "monospace" -> Monospace
        "cursive" -> Cursive
        else -> Default
    }
}

internal fun FontWeight.Companion.get(name: String): FontWeight {
    return when (name.lowercase()) {
        "thin" -> Thin
        "extralight" -> ExtraLight
        "light" -> Light
        "medium" -> Medium
        "semibold" -> SemiBold
        "bold" -> Bold
        "extrabold" -> ExtraBold
        "black" -> Black
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
