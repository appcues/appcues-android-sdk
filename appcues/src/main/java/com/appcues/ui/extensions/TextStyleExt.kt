package com.appcues.ui.extensions

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.appcues.data.model.ExperiencePrimitive.TextSpanPrimitive
import com.appcues.data.model.styling.ComponentStyle

@Composable
internal fun TextStyle.applyStyle(style: ComponentStyle): TextStyle {
    return copy(
        color = style.foregroundColor.getColor(isSystemInDarkTheme()) ?: color,
        fontSize = style.fontSize?.sp ?: fontSize,
        lineHeight = style.lineHeight?.sp ?: lineHeight,
        textAlign = style.getTextAlignment() ?: textAlign,
        fontFamily = style.getFontFamily() ?: fontFamily,
        letterSpacing = style.letterSpacing?.sp ?: letterSpacing,
        fontWeight = style.getFontWeight() ?: fontWeight,
    )
}

@Composable
internal fun List<TextSpanPrimitive>.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        forEach {
            withStyle(style = it.style.toSpanStyle()) {
                append(it.text)
            }
        }
    }
}

@Composable
private fun ComponentStyle.toSpanStyle(): SpanStyle {
    return SpanStyle(
        color = foregroundColor.getColor(isSystemInDarkTheme()) ?: Color.Unspecified,
        fontSize = fontSize?.sp ?: TextUnit.Unspecified,
        fontFamily = getFontFamily(),
        letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
        fontWeight = getFontWeight(),
    )
}
