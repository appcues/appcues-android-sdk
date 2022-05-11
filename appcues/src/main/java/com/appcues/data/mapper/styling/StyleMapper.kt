package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.data.remote.response.styling.StyleGradientColorResponse
import com.appcues.data.remote.response.styling.StyleResponse

internal class StyleMapper(
    private val styleColorMapper: StyleColorMapper = StyleColorMapper(),
    private val styleShadowMapper: StyleShadowMapper = StyleShadowMapper(),
) {

    fun map(from: StyleResponse?) = if (from != null) ComponentStyle(
        width = from.width,
        height = from.height,
        marginLeading = from.marginLeading,
        marginTop = from.marginTop,
        marginTrailing = from.marginTrailing,
        marginBottom = from.marginBottom,
        paddingLeading = from.paddingLeading,
        paddingTop = from.paddingTop,
        paddingBottom = from.paddingBottom,
        paddingTrailing = from.paddingTrailing,
        cornerRadius = from.cornerRadius,
        foregroundColor = styleColorMapper.map(from.foregroundColor),
        backgroundColor = styleColorMapper.map(from.backgroundColor),
        shadow = styleShadowMapper.map(from.shadow),
        // Not dealing with direction, every gradient is horizontal from start to end
        backgroundGradient = from.backgroundGradient.toComponentColorList(),
        borderColor = styleColorMapper.map(from.borderColor),
        borderWidth = from.borderWidth,
        fontName = from.fontName,
        fontSize = from.fontSize,
        letterSpacing = from.letterSpacing,
        lineHeight = from.lineHeight,
        textAlignment = from.textAlignment.toComponentHorizontalAlignment(),
        verticalAlignment = from.verticalAlignment.toComponentVerticalAlignment(),
        horizontalAlignment = from.horizontalAlignment.toComponentHorizontalAlignment(),
    ) else ComponentStyle()

    private fun String?.toComponentVerticalAlignment(): ComponentVerticalAlignment? {
        return when (this) {
            "top" -> ComponentVerticalAlignment.TOP
            "center" -> ComponentVerticalAlignment.CENTER
            "bottom" -> ComponentVerticalAlignment.BOTTOM
            else -> null
        }
    }

    private fun String?.toComponentHorizontalAlignment(): ComponentHorizontalAlignment? {
        return when (this) {
            "leading" -> ComponentHorizontalAlignment.LEADING
            "trailing" -> ComponentHorizontalAlignment.TRAILING
            "center" -> ComponentHorizontalAlignment.CENTER
            else -> null
        }
    }

    private fun StyleGradientColorResponse?.toComponentColorList(): List<ComponentColor>? {
        if (this == null) return null

        return arrayListOf<ComponentColor>().apply {
            colors.forEach { fromColor ->
                styleColorMapper.map(fromColor)?.let { add(it) }
            }
        }
    }
}
