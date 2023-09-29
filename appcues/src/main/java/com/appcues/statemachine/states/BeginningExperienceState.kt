package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.PresentationEffect

internal data class BeginningExperienceState(val experience: Experience) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int?
        get() = null

    override fun take(action: Action): Transition? {
        return if (action is StartStep) {
            toBeginningStep()
        } else null
    }

    private fun toBeginningStep(): Transition {
        // This is a safeguard against trying to load a step container that has zero steps.
        // We already guard against loading an experience with zero steps (groups) in the BeginningExperience
        // transition above. However, if a group exists, but has zero steps within - it will get here and
        // fail, so that we don't launch an AppcuesComposition and then have no content to render
        experience.stepContainers.firstOrNull()?.let {
            if (it.steps.isEmpty()) {
                return exit(StepError(experience, 0, "unable to render empty experience"))
            }
        }

        return next(
            state = BeginningStepState(experience, 0, true),
            sideEffect = PresentationEffect(experience, 0, 0, true)
        )
    }
}
