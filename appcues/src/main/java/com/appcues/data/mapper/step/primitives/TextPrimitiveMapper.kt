package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse

internal class TextPrimitiveMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: TextPrimitiveResponse) = with(from) {
        TextPrimitive(
            id = id,
            text = text,
            style = styleMapper.map(style),
        )
    }
}
