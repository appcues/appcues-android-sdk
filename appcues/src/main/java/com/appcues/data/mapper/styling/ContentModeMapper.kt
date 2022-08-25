package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentContentMode

internal fun mapComponentContentMode(value: String?) = when (value) {
    "fill" -> ComponentContentMode.FILL
    "fit" -> ComponentContentMode.FIT
    else -> ComponentContentMode.FIT
}
