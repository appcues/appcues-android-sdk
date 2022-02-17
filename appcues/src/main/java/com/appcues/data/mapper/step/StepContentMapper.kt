package com.appcues.data.mapper.step

import com.appcues.data.mapper.AppcuesMapperException
import com.appcues.data.mapper.step.primitives.ButtonMapper
import com.appcues.data.mapper.step.primitives.ImageMapper
import com.appcues.data.mapper.step.primitives.StackMapper
import com.appcues.data.mapper.step.primitives.TextMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.remote.response.step.StepContentResponse

internal class StepContentMapper(
    private val stackMapper: StackMapper = StackMapper(),
    private val textMapper: TextMapper = TextMapper(),
    private val buttonMapper: ButtonMapper = ButtonMapper(),
    private val imageMapper: ImageMapper = ImageMapper(),
) {

    fun map(from: StepContentResponse): ExperiencePrimitive = when (from.type) {
        "stack" -> stackMapper.map(from) { map(it) }
        "text" -> textMapper.map(from)
        "button" -> buttonMapper.map(from) { map(it) }
        "image" -> imageMapper.map(from)
        else -> from.mapContent()
    }

    private fun StepContentResponse.mapContent(): ExperiencePrimitive {
        requireNotNull(content) { throw AppcuesMapperException("$type($id) content is null") }

        return map(content)
    }
}
