package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.PresentationEffect

internal data class BeginningExperienceState(val experience: Experience) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int?
        get() = null

    override fun take(action: Action): Transition? {
        return if (action is StartExperience) {
            toBeginningStep()
        } else null
    }

    private fun toBeginningStep(): Transition {
        return next(
            state = BeginningStepState(experience, 0, true),
            sideEffect = PresentationEffect(experience, 0, 0, true)
        )
    }
}
