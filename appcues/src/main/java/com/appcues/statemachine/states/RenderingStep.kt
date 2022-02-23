package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Transition

internal class RenderingStep(
    override val scopeId: String,
    override val experience: Experience,
    val step: Int
) : State {
    override fun handleAction(action: Action): Transition? {
        return when (action) {
            is StartStep -> {
                // this is when the user advances forward/backward
                // might move to a page within current container
                // might progress to EndingStep then move to next

                // for now, we'll just treat it as ending and move to new step
                Transition(EndingStep(scopeId, experience, step, true), StartStep(action.step))
            }
            is EndExperience -> {
                // this means the experience was closed / dismissed
                Transition(EndingStep(scopeId, experience, step, true))
            }
            else -> null
        }
    }
}
