package com.appcues.data.mapper.action

import com.appcues.action.ActionRegistry
import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.model.Action
import com.appcues.data.model.RenderContext
import com.appcues.data.remote.appcues.response.action.ActionResponse
import java.util.UUID

internal class ActionsMapper(
    private val actionRegistry: ActionRegistry
) {

    fun map(renderContext: RenderContext, actions: Map<UUID, List<ActionResponse>>?, primitiveId: UUID): List<Action> {
        return arrayListOf<Action>().apply {
            // get possible action list based on id, then map to Action model
            actions?.get(primitiveId)?.forEach { actionResponse -> actionResponse.toAction(renderContext, actionRegistry)?.let { add(it) } }
        }
    }

    fun map(renderContext: RenderContext, from: Map<UUID, List<ActionResponse>>?): Map<UUID, List<Action>> {
        if (from == null) return hashMapOf()

        return hashMapOf<UUID, List<Action>>().apply {
            from.forEach { entry ->
                entry.value.mapNotNull { actionResponse -> actionResponse.toAction(renderContext, actionRegistry) }
                    .also { set(entry.key, it) }
            }
        }
    }
}

internal fun ActionResponse.toAction(renderContext: RenderContext, actionRegistry: ActionRegistry): Action? {
    return actionRegistry[type]?.let {
        Action(
            on = when (on) {
                "tap" -> Action.Trigger.TAP
                "longPress" -> Action.Trigger.LONG_PRESS
                "navigate" -> Action.Trigger.NAVIGATE
                else -> throw AppcuesMappingException("on property $on is unknown")
            },
            experienceAction = it.invoke(config, renderContext)
        )
    }
}
