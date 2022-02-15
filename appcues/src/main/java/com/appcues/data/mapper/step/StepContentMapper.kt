package com.appcues.data.mapper.step

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.step.primitives.ButtonMapper
import com.appcues.data.mapper.step.primitives.ImageMapper
import com.appcues.data.mapper.step.primitives.StackMapper
import com.appcues.data.mapper.step.primitives.TextMapper
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent
import java.util.UUID

internal class StepContentMapper(
    private val stackMapper: StackMapper = StackMapper(),
    private val textMapper: TextMapper = TextMapper(),
    private val buttonMapper: ButtonMapper = ButtonMapper(),
    private val imageMapper: ImageMapper = ImageMapper(),
) {

    fun map(from: StepContentResponse, actions: HashMap<UUID, List<ActionResponse>>?): ExperienceComponent = when (from.type) {
        "stack" -> stackMapper.map(from, actions) { map(it, actions) }
        "text" -> textMapper.map(from, actions)
        "button" -> buttonMapper.map(from, actions) { map(it, actions) }
        "image" -> imageMapper.map(from, actions)
        else -> from.mapContent(actions)
    }

    private fun StepContentResponse.mapContent(actions: HashMap<UUID, List<ActionResponse>>?): ExperienceComponent {
        requireNotNull(content) { throw AppcuesMappingException("$type($id) content is null") }

        return map(content, actions)
    }
}
