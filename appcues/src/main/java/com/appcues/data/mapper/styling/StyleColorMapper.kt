package com.appcues.data.mapper.styling

import com.appcues.data.remote.response.styling.StyleColorResponse
import com.appcues.domain.entity.styling.ComponentColor

internal class StyleColorMapper {

    fun map(from: StyleColorResponse?): ComponentColor? {
        if (from == null) return null

        val light = normalizeToArgbLong(from.light)
        val dark = from.dark?.let { normalizeToArgbLong(it) } ?: light

        return ComponentColor(
            light = light,
            dark = dark,
        )
    }
}
