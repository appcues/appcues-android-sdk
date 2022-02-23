package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Transition

internal class Failing(
    override val scopeId: String,
    override val experience: Experience?
) : State {
    override fun handleAction(action: Action): Transition? {
        return when (action) {
            is Reset -> {
                Transition(Idling(scopeId))
            }
            else -> null
        }
    }
}
