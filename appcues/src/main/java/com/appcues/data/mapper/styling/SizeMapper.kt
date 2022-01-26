package com.appcues.data.mapper.styling

import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.domain.entity.styling.ComponentSize

internal class SizeMapper {

    fun map(from: SizeResponse) = ComponentSize(
        width = from.width,
        height = from.height,
    )
}
