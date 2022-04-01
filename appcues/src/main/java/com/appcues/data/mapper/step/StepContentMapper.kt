package com.appcues.data.mapper.step

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.step.primitives.ButtonPrimitiveMapper
import com.appcues.data.mapper.step.primitives.EmbedPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ImagePrimitiveMapper
import com.appcues.data.mapper.step.primitives.StackPrimitiveMapper
import com.appcues.data.mapper.step.primitives.TextPrimitiveMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.remote.response.step.StepContentResponse

internal class StepContentMapper(
    private val stackMapper: StackPrimitiveMapper,
    private val textMapper: TextPrimitiveMapper,
    private val buttonMapper: ButtonPrimitiveMapper,
    private val imageMapper: ImagePrimitiveMapper,
    private val embedMapper: EmbedPrimitiveMapper,
) {

    fun map(from: StepContentResponse): ExperiencePrimitive = when (from.type) {
        "stack" -> stackMapper.map(from) { map(it) }
        "text" -> textMapper.map(from)
        "button" -> buttonMapper.map(from) { map(it) }
        "image" -> imageMapper.map(from)
        "embed" -> embedMapper.map(from)
        else -> from.mapContent()
    }

    private fun StepContentResponse.mapContent(): ExperiencePrimitive {
        requireNotNull(content) { throw AppcuesMappingException("$type($id) content is null") }

        return map(content)
    }
}
