package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.ExperiencePackage
import com.appcues.statemachine.State

internal class EndingExperience(val experience: ExperiencePackage) : State {

    override fun handleAction(action: Action): State {
        return when (action) {
            is Reset -> {
                Idling()
            }
            else -> this
        }
    }
}
