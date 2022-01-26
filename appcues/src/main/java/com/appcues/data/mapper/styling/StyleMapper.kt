package com.appcues.data.mapper.styling

import com.appcues.data.remote.response.styling.StyleResponse
import com.appcues.domain.entity.styling.ComponentStyle

internal class StyleMapper {

    fun map(from: StyleResponse?) = if (from != null) ComponentStyle(
        marginLeading = from.marginLeading,
        marginTop = from.marginTop,
        marginTrailing = from.marginTrailing,
        marginBottom = from.marginBottom,
        paddingLeading = from.paddingLeading,
        paddingTop = from.paddingTop,
        paddingBottom = from.paddingBottom,
        paddingTrailing = from.paddingTrailing,
        cornerRadius = from.cornerRadius,
    ) else ComponentStyle()
}
