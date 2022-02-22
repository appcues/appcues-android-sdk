package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State

internal class EndingStep(
    override val scopeId: String,
    override val experience: Experience,
    val step: Int,
    val dismissContainer: Boolean
) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is StartStep -> {
                // trait packaging?...
                BeginningStep(scopeId, experience, action.step)
                    // auto-invoke rendering
                    .handleAction(RenderStep())
            }
            is EndExperience -> {
                EndingExperience(scopeId, experience)
                    .handleAction(Reset())
            }
            else -> this
        }
    }
}
