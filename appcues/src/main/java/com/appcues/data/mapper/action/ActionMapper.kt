package com.appcues.data.mapper.action

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.domain.entity.action.Action
import com.appcues.domain.entity.action.OnAction

internal class ActionMapper {

    fun map(from: ActionResponse) = Action(
        on = when (from.on) {
            "tap" -> OnAction.TAP
            "longPress" -> OnAction.LONG_PRESS
            else -> throw AppcuesMappingException("on property ${from.on} is unknown")
        },
        type = from.type,
    )
}
