package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.ExperiencePackage
import com.appcues.statemachine.State

internal class BeginningExperience(val experience: ExperiencePackage) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is StartStep -> {
                // trait packaging?...
                // transition to begin step
                BeginningStep(experience, action.step)
                    // and then auto-invoke rendering
                    .handleAction(RenderStep())
            }
            else -> this
        }
    }
}
