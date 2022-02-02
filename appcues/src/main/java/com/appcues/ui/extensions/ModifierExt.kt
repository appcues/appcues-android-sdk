package com.appcues.ui.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.domain.entity.styling.ComponentStyle

internal fun Modifier.componentStyle(style: ComponentStyle, isDark: Boolean, defaultBackgroundColor: Color? = null) = this.then(
    Modifier
        .padding(style.getMargins())
        .componentSize(style)
        .componentCorner(style)
        .componentBorder(style, isDark)
        .componentBackground(style, isDark, defaultBackgroundColor)
        .padding(style.getPaddings())
)

internal fun Modifier.componentBackground(
    style: ComponentStyle,
    isDark: Boolean,
    defaultColor: Color? = null
) = this.then(
    when {
        style.backgroundGradient != null -> Modifier.background(
            Brush.horizontalGradient(style.backgroundGradient.map { Color(if (isDark) it.dark else it.light) })
        )
        style.backgroundColor != null -> Modifier.background(style.backgroundColor.getColor(isDark))
        defaultColor != null -> Modifier.background(defaultColor)
        else -> Modifier
    }
)

internal fun Modifier.componentBorder(
    style: ComponentStyle,
    isDark: Boolean
) = this.then(
    if (style.borderWidth != null && style.borderWidth != 0 && style.borderColor != null) {
        Modifier
            .border(style.borderWidth.dp, style.borderColor.getColor(isDark), RoundedCornerShape(style.cornerRadius))
    } else {
        Modifier
    }
)

internal fun Modifier.componentSize(style: ComponentStyle) = this.then(
    when {
        style.width != null && style.height != null -> Modifier.size(style.width.dp, style.height.dp)
        style.width != null -> Modifier.size(style.width.dp, Dp.Unspecified)
        style.height != null -> Modifier.size(Dp.Unspecified, style.height.dp)
        else -> Modifier
    }
)

internal fun Modifier.componentCorner(style: ComponentStyle) = this.then(
    when {
        style.cornerRadius != 0 -> Modifier.clip(RoundedCornerShape(style.cornerRadius))
        else -> Modifier
    }
)
