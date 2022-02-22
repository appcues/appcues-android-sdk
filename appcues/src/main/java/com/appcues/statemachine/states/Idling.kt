package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State

internal class Idling(
    override val scopeId: String,
    override val experience: Experience? = null
) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is StartExperience -> {
                // no work here, transition state
                BeginningExperience(scopeId, action.experience)
                    // and auto-invoke starting step 0
                    .handleAction(StartStep(0))
            }
            else -> this
        }
    }
}
