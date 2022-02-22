package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.State

internal class EndingExperience(
    override val scopeId: String,
    override val experience: Experience
) : State {

    override fun handleAction(action: Action): State {
        return when (action) {
            is Reset -> {
                Idling(scopeId)
            }
            else -> this
        }
    }
}
