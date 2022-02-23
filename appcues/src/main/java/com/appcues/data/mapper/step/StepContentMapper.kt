package com.appcues.data.mapper.step

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.step.primitives.ButtonPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ImagePrimitiveMapper
import com.appcues.data.mapper.step.primitives.StackPrimitiveMapper
import com.appcues.data.mapper.step.primitives.TextPrimitiveMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import java.util.UUID

internal class StepContentMapper(
    private val stackMapper: StackPrimitiveMapper,
    private val textMapper: TextPrimitiveMapper,
    private val buttonMapper: ButtonPrimitiveMapper,
    private val imageMapper: ImagePrimitiveMapper
) {

    fun map(from: StepContentResponse, actions: HashMap<UUID, List<ActionResponse>>?): ExperiencePrimitive = when (from.type) {
        "stack" -> stackMapper.map(from, actions) { map(it, actions) }
        "text" -> textMapper.map(from, actions)
        "button" -> buttonMapper.map(from, actions) { map(it, actions) }
        "image" -> imageMapper.map(from, actions)
        else -> from.mapContent(actions)
    }

    private fun StepContentResponse.mapContent(actions: HashMap<UUID, List<ActionResponse>>?): ExperiencePrimitive {
        requireNotNull(content) { throw AppcuesMappingException("$type($id) content is null") }

        return map(content, actions)
    }
}
