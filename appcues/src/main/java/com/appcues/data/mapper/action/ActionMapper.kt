package com.appcues.data.mapper.action

import com.appcues.data.mapper.AppcuesMapperException
import com.appcues.data.model.action.Action
import com.appcues.data.model.action.OnAction.LONG_PRESS
import com.appcues.data.model.action.OnAction.TAP
import com.appcues.data.remote.response.action.ActionResponse

internal class ActionMapper {

    fun map(from: ActionResponse) = Action(
        on = when (from.on) {
            "tap" -> TAP
            "longPress" -> LONG_PRESS
            else -> throw AppcuesMapperException("on property ${from.on} is unknown")
        },
        type = from.type,
    )
}
