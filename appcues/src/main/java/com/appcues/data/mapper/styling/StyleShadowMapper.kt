package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentShadow
import com.appcues.data.remote.response.styling.StyleShadowResponse

internal class StyleShadowMapper(
    private val styleColorMapper: StyleColorMapper = StyleColorMapper(),
) {

    fun map(from: StyleShadowResponse?): ComponentShadow? {
        if (from == null) return null

        val componentColor = styleColorMapper.map(from.color) ?: return null

        return ComponentShadow(
            color = componentColor,
            radius = from.radius,
            x = from.x,
            y = from.y,
        )
    }
}
