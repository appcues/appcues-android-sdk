package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.State

// not sure if we really need this state - more of a pass through for analytics on the way back to Idling?
internal class Failing(
    override val scopeId: String,
    override val experience: Experience?
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
