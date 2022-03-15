package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition

internal class EndingExperience(
    override val experience: Experience
) : State {

    override fun handleAction(action: Action): Transition? {
        return when (action) {
            is StartExperience -> Transition.ExperienceActiveError(experience)
            is Reset -> {
                Transition(Idling())
            }
            else -> null
        }
    }
}
