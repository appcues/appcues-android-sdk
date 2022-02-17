package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMapperException
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.remote.response.step.StepContentResponse

internal class TextMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: StepContentResponse): ExperiencePrimitive {
        return with(from) {
            requireNotNull(text) { throw AppcuesMapperException("text($id) text is null") }

            TextPrimitive(
                id = id,
                text = text,
                style = styleMapper.map(style),
            )
        }
    }
}
