package com.appcues.ui.extensions

import android.content.Context
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.appcues.data.model.styling.ComponentStyle

internal fun TextStyle.applyStyle(style: ComponentStyle, context: Context, isDark: Boolean): TextStyle {
    return copy(
        color = style.foregroundColor.getColor(isDark) ?: color,
        fontSize = style.fontSize?.sp ?: fontSize,
        lineHeight = style.lineHeight?.sp ?: lineHeight,
        textAlign = style.getTextAlignment() ?: textAlign,
        fontFamily = style.getFontFamily(context) ?: fontFamily,
        letterSpacing = style.letterSpacing?.sp ?: letterSpacing,
        fontWeight = style.getFontWeight() ?: fontWeight,
    )
}
