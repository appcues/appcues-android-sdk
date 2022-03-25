package com.appcues.ui.extensions

import android.content.Context
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
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.BLACK
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.BOLD
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.HEAVY
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.LIGHT
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.MEDIUM
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.REGULAR
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.SEMI_BOLD
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.THIN
import com.appcues.data.model.styling.ComponentStyle.ComponentFontWeight.ULTRA_LIGHT
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
        val fontId = context.resources.getIdentifier(fontName, "font", context.packageName)
        if (fontId != 0) {
            return FontFamily(Font(fontId))
        }
    }
    return null
}

internal fun ComponentStyle.getFontWeight(): FontWeight? {
    return when (fontWeight) {
        ULTRA_LIGHT -> FontWeight.ExtraLight
        THIN -> FontWeight.Thin
        LIGHT -> FontWeight.Light
        REGULAR -> FontWeight.Normal
        MEDIUM -> FontWeight.Medium
        SEMI_BOLD -> FontWeight.SemiBold
        BOLD -> FontWeight.Bold
        HEAVY -> FontWeight.ExtraBold
        BLACK -> FontWeight.Black
        null -> null
    }
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
