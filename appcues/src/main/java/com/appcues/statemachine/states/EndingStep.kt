package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
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
                // trait packaging?...
                Transition(BeginningStep(scopeId, experience, action.step), RenderStep())
            }
            is EndExperience -> {
                Transition(EndingExperience(scopeId, experience), Reset())
            }
            else -> null
        }
    }
}
