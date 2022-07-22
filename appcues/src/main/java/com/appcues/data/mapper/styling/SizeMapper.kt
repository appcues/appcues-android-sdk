package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentSize
import com.appcues.data.remote.response.styling.StyleSizeResponse

internal class SizeMapper {

    fun map(from: StyleSizeResponse) = ComponentSize(
        width = from.width,
        height = from.height,
    )
}
