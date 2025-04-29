package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.ExperienceActionEffect

// trackAnalytics disables analytics used on force stop
internal data class EndingExperienceState(
    val experience: Experience,
    val flatStepIndex: Int,
    val markComplete: Boolean,
) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int
        get() = flatStepIndex

    override fun take(action: Action): Transition? {
        return if (action is Reset) {
            fromEndingExperienceToIdling()
        } else null
    }

    private fun fromEndingExperienceToIdling(): Transition {
        return next(
            state = IdlingState,
            sideEffect = if (markComplete) ExperienceActionEffect(experience.completionActions) else null
        )
    }
}
