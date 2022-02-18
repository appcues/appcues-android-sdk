package com.appcues.statemachine

import android.content.Context
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.states.Idling

internal class StateMachine(
    val scopeId: String,
    val context: Context
) {
    private var currentState: State = Idling()

    // main entry point to start rendering a new experience
    // will only work if the system is in a state ready to begin showing
    // something new - i.e. Idling - can't show an experience if another is showing
    fun showExperience(experience: Experience) {
        currentState = currentState.handleAction(
            StartExperience(
                ExperiencePackage(context, scopeId, experience)
            )
        )
    }

    // to be used later by ExperienceAction to advance to next/prev step, or step by index/id
    fun showStepInCurrentExperience(step: Int) {
        currentState = currentState.handleAction(StartStep(step))
    }

    // signaled when the AppcuesActivity completes - we'll need a way to know whether to
    // move on to next step, or dismiss the experience.
    // for now, will just dismiss/reset
    fun endExperience() {
        currentState = currentState.handleAction(EndExperience())
    }
}
