package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.action.ActionMapMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import java.util.UUID

internal class ButtonMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
    private val actionMapMapper: ActionMapMapper = ActionMapMapper(),
) {

    fun map(
        from: StepContentResponse,
        actions: HashMap<UUID, List<ActionResponse>>?,
        blockTransform: (StepContentResponse) -> ExperienceComponent
    ): ButtonComponent {
        return with(from) {
            requireNotNull(content) { throw AppcuesMappingException("button($id) content is null") }

            ButtonComponent(
                id = id,
                content = blockTransform(content),
                style = styleMapper.map(style),
                actions = actionMapMapper.map(actions, id).toMutableList().apply {
                    addAll(actionMapMapper.map(actions, content.id))
                },
            )
        }
    }
}
