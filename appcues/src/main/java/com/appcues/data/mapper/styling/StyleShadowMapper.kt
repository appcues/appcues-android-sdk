package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentShadow
import com.appcues.data.remote.response.styling.StyleShadowResponse

internal fun StyleShadowResponse.mapComponentShadow(): ComponentShadow? {
    val componentColor = color.mapComponentColor()

    return ComponentShadow(
        color = componentColor,
        radius = radius,
        x = x,
        y = y,
    )
}
