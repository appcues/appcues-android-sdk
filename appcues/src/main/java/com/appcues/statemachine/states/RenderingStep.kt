package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State

internal class RenderingStep(
    override val scopeId: String,
    override val experience: Experience,
    val step: Int
) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is StartStep -> {
                // this is when the user advances forward/backward
                // might move to a page within current container
                // might progress to EndingStep then move to next

                // for now, we'll just treat it as ending and move to new step
                // TBD - dismiss current step?
                val dismiss = true // did we finish the last step of the container?
                EndingStep(scopeId, experience, step, dismiss)
                    .handleAction(StartStep(action.step))
            }
//            is EndStep -> {
//                // this transition from render -> endstep ends up ending the experience on iOS
//                // but also says its not currently in use
//                EndingStep(scopeId, experience, step)
//                    .handleAction(EndExperience())
//                    .handleAction(Reset())
//            }
            is EndExperience -> {
                // this means the experience was closed / dismissed
                EndingStep(scopeId, experience, step, true)
            }
            else -> this
        }
    }
}
