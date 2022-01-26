package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.StyleColorMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.styling.StyleResponse
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.domain.entity.styling.ComponentColor

internal class ButtonMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
    private val styleColorMapper: StyleColorMapper = StyleColorMapper(),
) {

    fun map(from: StepContentResponse, blockTransform: (StepContentResponse) -> ExperienceComponent): ButtonComponent {
        return with(from) {
            requireNotNull(content) { throw AppcuesMappingException("button($id) content is null") }

            ButtonComponent(
                id = id,
                backgroundColors = style.getBackgroundColors(),
                content = blockTransform(content),
                style = styleMapper.map(style)
            )
        }
    }

    private fun StyleResponse?.getBackgroundColors() = arrayListOf<ComponentColor>().apply {
        if (this@getBackgroundColors != null) {
            if (backgroundGradient != null) {
                addAll(backgroundGradient.colors.map { styleColorMapper.map(it) })
            } else if (backgroundColor != null) {
                add(styleColorMapper.map(backgroundColor))
            }
        }
    }
}
