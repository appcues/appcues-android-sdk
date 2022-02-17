package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentSize
import com.appcues.data.remote.response.styling.SizeResponse

internal class SizeMapper {

    fun map(from: SizeResponse) = ComponentSize(
        width = from.width,
        height = from.height,
    )
}
