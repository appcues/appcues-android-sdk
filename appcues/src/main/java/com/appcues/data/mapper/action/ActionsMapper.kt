package com.appcues.data.mapper.action

import com.appcues.action.ActionRegistry
import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.model.Action
import com.appcues.data.remote.response.action.ActionResponse
import java.util.UUID

internal class ActionsMapper(
    private val actionRegistry: ActionRegistry
) {

    fun map(actions: HashMap<UUID, List<ActionResponse>>?, primitiveId: UUID): List<Action> {
        return arrayListOf<Action>().apply {
            actions?.get(primitiveId)?.forEach { actionResponse -> actionResponse.toAction()?.let { add(it) } }
        }
    }

    private fun ActionResponse.toAction(): Action? {
        return actionRegistry[type]?.let {
            Action(
                on = when (on) {
                    "tap" -> Action.Motion.TAP
                    "longPress" -> Action.Motion.TAP
                    else -> throw AppcuesMappingException("on property $on is unknown")
                },
                experienceAction = it.invoke(config)
            )
        }
    }
}
