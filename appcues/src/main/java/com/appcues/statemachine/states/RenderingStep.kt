package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.EndStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.ExperiencePackage
import com.appcues.statemachine.State

internal class RenderingStep(val experience: ExperiencePackage, val step: Int) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is StartStep -> {
                // this is when the user advances forward/backward
                // might move to a page within current container
                // might progress to EndingStep then move to next

                // for now, we'll just treat it as ending and move to new step
                // TBD - dismiss current step?
                EndingStep(experience, step)
                    .handleAction(StartStep(action.step))
            }
            is EndStep -> {
                // this transition from render -> endstep ends up ending the experience on iOS
                // but also says its not currently in use
                EndingStep(experience, step)
                    .handleAction(EndExperience())
                    .handleAction(Reset())
            }
            is EndExperience -> {
                // this means the activity was dismissed - process through end step
                EndingStep(experience, step)
                    // then auto-invoke end experience, which will process back to Idle
                    .handleAction(EndExperience())
                    .handleAction(Reset())
            }
            else -> this
        }
    }
}
