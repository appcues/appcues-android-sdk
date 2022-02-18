package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.State

// not sure if we really need this state - more of a pass through for analytics on the way back to Idling?
internal class Failing : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is Reset -> {
                Idling()
            }
            else -> this
        }
    }
}
