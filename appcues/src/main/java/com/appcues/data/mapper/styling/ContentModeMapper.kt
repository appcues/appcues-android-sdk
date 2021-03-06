package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentContentMode

internal class ContentModeMapper {

    fun map(from: String?): ComponentContentMode {
        return when (from) {
            "fill" -> ComponentContentMode.FILL
            "fit" -> ComponentContentMode.FIT
            else -> ComponentContentMode.FIT
        }
    }
}
