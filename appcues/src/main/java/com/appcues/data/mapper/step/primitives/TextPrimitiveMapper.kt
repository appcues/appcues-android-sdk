package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.remote.response.step.StepContentResponse

internal class TextPrimitiveMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: StepContentResponse): TextPrimitive {
        return with(from) {
            requireNotNull(text) { throw AppcuesMappingException("text($id) text is null") }

            TextPrimitive(
                id = id,
                text = text,
                style = styleMapper.map(style),
            )
        }
    }
}
