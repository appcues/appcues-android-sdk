package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.data.model.StepReference.StepIndex
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.ContinuationEffect

internal object IdlingState : State {

    override val currentExperience: Experience?
        get() = null
    override val currentStepIndex: Int?
        get() = null

    override fun take(action: Action): Transition? {
        return if (action is StartExperience) {
            toBeginningExperience(action)
        } else null
    }

    private fun toBeginningExperience(action: StartExperience): Transition {
        return if (!action.experience.error.isNullOrEmpty()) {
            keep(ExperienceError(action.experience, action.experience.error))
        } else if (action.experience.stepContainers.isEmpty()) {
            keep(ExperienceError(action.experience, "Experience has 0 steps"))
        } else {
            next(BeginningExperienceState(action.experience), ContinuationEffect(StartStep(StepIndex(0))))
        }
    }
}
