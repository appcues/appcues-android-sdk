package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentSize
import com.appcues.data.remote.appcues.response.styling.StyleSizeResponse

internal fun StyleSizeResponse?.mapComponentSize(): ComponentSize? {
    if (this == null) return null

    return ComponentSize(
        width = width,
        height = height,
    )
}
