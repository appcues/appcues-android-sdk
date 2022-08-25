package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment

internal fun mapComponentVerticalAlignment(value: String?): ComponentVerticalAlignment? {
    return when (value) {
        "top" -> ComponentVerticalAlignment.TOP
        "center" -> ComponentVerticalAlignment.CENTER
        "bottom" -> ComponentVerticalAlignment.BOTTOM
        else -> null
    }
}

internal fun mapComponentHorizontalAlignment(value: String?): ComponentHorizontalAlignment? {
    return when (value) {
        "leading" -> ComponentHorizontalAlignment.LEADING
        "trailing" -> ComponentHorizontalAlignment.TRAILING
        "center" -> ComponentHorizontalAlignment.CENTER
        else -> null
    }
}
