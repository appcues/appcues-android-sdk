package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.State

internal class BeginningStep(
    override val scopeId: String,
    override val experience: Experience,
    val step: Int
) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is RenderStep -> {
                // this transition is triggered by "callback" from AppcuesActivity (via VM)
                // to tell us that the view has rendered

                // no additional work to do, just update state
                RenderingStep(scopeId, experience, step)
            }
            else -> this
        }
    }
}
