package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Transition

internal class Idling(
    override val scopeId: String,
    override val experience: Experience? = null
) : State {
    override fun handleAction(action: Action): Transition? {
        return when (action) {
            is StartExperience -> {
                // no work here, transition state
                Transition(BeginningExperience(scopeId, action.experience), StartStep(0))
            }
            else -> null
        }
    }
}
