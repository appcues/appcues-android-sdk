package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Transition

internal class EndingStep(
    override val scopeId: String,
    override val experience: Experience,
    val step: Int,
    val dismissContainer: Boolean
) : State {
    override fun handleAction(action: Action): Transition? {
        return when (action) {
            is StartStep -> {
                // would either move page forward in existing container or start a new activity - TBD
                Transition(BeginningStep(scopeId, experience, action.step))
            }
            is EndExperience -> {
                Transition(EndingExperience(scopeId, experience), Reset())
            }
            else -> null
        }
    }
}
