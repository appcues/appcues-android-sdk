package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.remote.response.styling.StyleColorResponse

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
