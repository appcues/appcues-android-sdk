package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent

internal class ButtonMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: StepContentResponse, blockTransform: (StepContentResponse) -> ExperienceComponent): ButtonComponent {
        return with(from) {
            requireNotNull(content) { throw AppcuesMappingException("button($id) content is null") }

            ButtonComponent(
                id = id,
                content = blockTransform(content),
                style = styleMapper.map(style)
            )
        }
    }
}
