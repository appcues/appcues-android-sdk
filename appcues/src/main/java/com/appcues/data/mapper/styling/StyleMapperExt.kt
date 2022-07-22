package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment

internal fun String?.toComponentVerticalAlignment(): ComponentVerticalAlignment? {
    return when (this) {
        "top" -> ComponentVerticalAlignment.TOP
        "center" -> ComponentVerticalAlignment.CENTER
        "bottom" -> ComponentVerticalAlignment.BOTTOM
        else -> null
    }
}

internal fun String?.toComponentHorizontalAlignment(): ComponentHorizontalAlignment? {
    return when (this) {
        "leading" -> ComponentHorizontalAlignment.LEADING
        "trailing" -> ComponentHorizontalAlignment.TRAILING
        "center" -> ComponentHorizontalAlignment.CENTER
        else -> null
    }
}
