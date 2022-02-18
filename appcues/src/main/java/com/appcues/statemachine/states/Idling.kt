package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State

internal class Idling : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is StartExperience -> {
                // no work here, transition state
                BeginningExperience(action.experience)
                    // and auto-invoke starting step 0
                    .handleAction(StartStep(0))
            }
            else -> this
        }
    }
}
