package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.TextComponent

internal class TextMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: StepContentResponse): ExperienceComponent {
        return with(from) {
            requireNotNull(text) { throw AppcuesMappingException("text($id) text is null") }

            TextComponent(
                id = id,
                text = text,
                style = styleMapper.map(style),
            )
        }
    }
}
