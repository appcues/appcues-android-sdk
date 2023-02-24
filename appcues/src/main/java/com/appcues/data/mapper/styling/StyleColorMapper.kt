package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.remote.appcues.response.styling.StyleColorResponse

internal fun StyleColorResponse.mapComponentColor(): ComponentColor {
    val light = normalizeToArgbLong(light)
    val dark = dark?.let { normalizeToArgbLong(it) } ?: light

    return ComponentColor(
        light = light,
        dark = dark,
    )
}
