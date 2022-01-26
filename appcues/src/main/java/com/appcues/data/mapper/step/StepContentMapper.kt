package com.appcues.data.mapper.step

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.step.primitives.ButtonMapper
import com.appcues.data.mapper.step.primitives.ImageMapper
import com.appcues.data.mapper.step.primitives.StackMapper
import com.appcues.data.mapper.step.primitives.TextMapper
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent

internal class StepContentMapper(
    private val stackMapper: StackMapper = StackMapper(),
    private val textMapper: TextMapper = TextMapper(),
    private val buttonMapper: ButtonMapper = ButtonMapper(),
    private val imageMapper: ImageMapper = ImageMapper(),
) {

    fun map(from: StepContentResponse): ExperienceComponent = when (from.type) {
        "stack" -> stackMapper.map(from) { map(it) }
        "text" -> textMapper.map(from)
        "button" -> buttonMapper.map(from) { map(it) }
        "image" -> imageMapper.map(from)
        else -> from.mapContent()
    }

    private fun StepContentResponse.mapContent(): ExperienceComponent {
        requireNotNull(content) { throw AppcuesMappingException("$type($id) content is null") }

        return map(content)
    }
}
