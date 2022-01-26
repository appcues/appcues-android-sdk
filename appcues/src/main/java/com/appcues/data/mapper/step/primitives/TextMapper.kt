package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.StyleColorMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.TextComponent

internal class TextMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
    private val styleColorMapper: StyleColorMapper = StyleColorMapper(),
) {

    fun map(from: StepContentResponse): ExperienceComponent {
        return with(from) {
            requireNotNull(style) { throw AppcuesMappingException("text($id) style is null") }
            requireNotNull(style.fontSize) { throw AppcuesMappingException("text($id) fontSize is null") }
            requireNotNull(style.foregroundColor) { throw AppcuesMappingException("text($id) foregroundColor is null") }
            requireNotNull(text) { throw AppcuesMappingException("text($id) text is null") }

            TextComponent(
                id = id,
                text = text,
                textSize = style.fontSize,
                textColor = styleColorMapper.map(style.foregroundColor),
                style = styleMapper.map(style)
            )
        }
    }
}
