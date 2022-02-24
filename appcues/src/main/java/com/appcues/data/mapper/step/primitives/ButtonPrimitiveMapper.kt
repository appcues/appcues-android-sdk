package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import java.util.UUID

internal class ButtonPrimitiveMapper(
    private val actionsMapper: ActionsMapper,
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(
        from: StepContentResponse,
        actions: HashMap<UUID, List<ActionResponse>>?,
        blockTransform: (StepContentResponse) -> ExperiencePrimitive
    ): ButtonPrimitive {
        return with(from) {
            requireNotNull(content) { throw AppcuesMappingException("button($id) content is null") }

            ButtonPrimitive(
                id = id,
                content = blockTransform(content),
                style = styleMapper.map(style),
                actions = actionsMapper.map(actions, id),
            )
        }
    }
}
