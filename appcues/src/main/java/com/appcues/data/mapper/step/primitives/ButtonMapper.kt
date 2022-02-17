package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMapperException
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.remote.response.step.StepContentResponse

internal class ButtonMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: StepContentResponse, blockTransform: (StepContentResponse) -> ExperiencePrimitive): ButtonPrimitive {
        return with(from) {
            requireNotNull(content) { throw AppcuesMapperException("button($id) content is null") }

            ButtonPrimitive(
                id = id,
                content = blockTransform(content),
                style = styleMapper.map(style)
            )
        }
    }
}
