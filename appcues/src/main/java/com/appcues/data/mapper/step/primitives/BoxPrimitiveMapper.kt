package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse

internal class BoxPrimitiveMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {
    fun map(
        from: BoxPrimitiveResponse,
        blockTransform: (PrimitiveResponse) -> ExperiencePrimitive
    ) = with(from) {
        BoxPrimitive(
            id = id,
            items = items.map { blockTransform(it) },
            style = styleMapper.map(style),
        )
    }
}
