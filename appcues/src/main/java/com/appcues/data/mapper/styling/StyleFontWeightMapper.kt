package com.appcues.data.mapper.styling

import com.appcues.domain.entity.styling.ComponentStyle.ComponentFontWeight

internal class StyleFontWeightMapper {

    fun map(from: String?): ComponentFontWeight? {
        return if (from.isNullOrEmpty()) null else when (from) {
            "ultraLight" -> ComponentFontWeight.ULTRA_LIGHT
            "thin" -> ComponentFontWeight.THIN
            "light" -> ComponentFontWeight.LIGHT
            "regular" -> ComponentFontWeight.REGULAR
            "medium" -> ComponentFontWeight.MEDIUM
            "semibold" -> ComponentFontWeight.SEMI_BOLD
            "bold" -> ComponentFontWeight.BOLD
            "heavy" -> ComponentFontWeight.HEAVY
            "black" -> ComponentFontWeight.BLACK

            else -> null
        }
    }
}
