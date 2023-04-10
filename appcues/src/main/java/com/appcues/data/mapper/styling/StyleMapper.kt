package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.remote.appcues.response.styling.StyleGradientColorResponse
import com.appcues.data.remote.appcues.response.styling.StyleResponse

internal fun StyleResponse?.mapComponentStyle(): ComponentStyle {
    if (this == null) return ComponentStyle()

    fun StyleGradientColorResponse?.toComponentColorList(): List<ComponentColor>? {
        if (this == null) return null
        return arrayListOf<ComponentColor>().apply {
            colors.forEach { fromColor ->
                add(fromColor.mapComponentColor())
            }
        }
    }
    return ComponentStyle(
        width = width,
        height = height,
        marginLeading = marginLeading,
        marginTop = marginTop,
        marginTrailing = marginTrailing,
        marginBottom = marginBottom,
        paddingLeading = paddingLeading?.coerceAtLeast(0.0),
        paddingTop = paddingTop?.coerceAtLeast(0.0),
        paddingBottom = paddingBottom?.coerceAtLeast(0.0),
        paddingTrailing = paddingTrailing?.coerceAtLeast(0.0),
        cornerRadius = cornerRadius?.coerceAtLeast(0.0),
        foregroundColor = foregroundColor?.mapComponentColor(),
        backgroundColor = backgroundColor?.mapComponentColor(),
        backgroundImage = backgroundImage?.mapComponentBackgroundImage(),
        shadow = shadow?.mapComponentShadow(),
        // Not dealing with direction, every gradient is horizontal from start to end
        backgroundGradient = backgroundGradient.toComponentColorList(),
        borderColor = borderColor?.mapComponentColor(),
        borderWidth = borderWidth?.coerceAtLeast(0.0),
        fontName = fontName,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        textAlignment = mapComponentHorizontalAlignment(textAlignment),
        verticalAlignment = mapComponentVerticalAlignment(verticalAlignment),
        horizontalAlignment = mapComponentHorizontalAlignment(horizontalAlignment),
    )
}
