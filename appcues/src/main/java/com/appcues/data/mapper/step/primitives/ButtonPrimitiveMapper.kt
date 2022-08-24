package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ButtonPrimitiveResponse

internal class ButtonPrimitiveMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {
    fun map(
        from: ButtonPrimitiveResponse,
        blockTransform: (PrimitiveResponse) -> ExperiencePrimitive
    ) = with(from) {
        ButtonPrimitive(
            id = id,
            content = blockTransform(content),
            style = styleMapper.map(style),
        )
    }
}
