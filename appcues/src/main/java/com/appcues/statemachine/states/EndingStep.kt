package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.ExperiencePackage
import com.appcues.statemachine.State

internal class EndingStep(val experience: ExperiencePackage, val step: Int) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is StartStep -> {
                // trait packaging?...
                BeginningStep(experience, action.step)
                    // auto-invoke rendering
                    .handleAction(RenderStep())
            }
            is EndExperience -> {
                EndingExperience(experience)
                    .handleAction(Reset())
            }
            else -> this
        }
    }
}
