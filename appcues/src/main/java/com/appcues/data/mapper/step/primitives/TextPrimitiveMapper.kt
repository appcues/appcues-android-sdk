package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import java.util.UUID

internal class TextPrimitiveMapper(
    private val actionsMapper: ActionsMapper,
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: StepContentResponse, actions: HashMap<UUID, List<ActionResponse>>?): TextPrimitive {
        return with(from) {
            requireNotNull(text) { throw AppcuesMappingException("text($id) text is null") }

            TextPrimitive(
                id = id,
                text = text,
                style = styleMapper.map(style),
                actions = actionsMapper.map(actions, id)
            )
        }
    }
}
