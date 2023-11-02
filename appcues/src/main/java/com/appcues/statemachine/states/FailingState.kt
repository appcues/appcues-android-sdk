package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.Retry
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.SideEffect
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.ContinuationEffect

internal data class FailingState(
    // this is the desired state to return to, if the failure can be recovered
    val targetState: State,
    val retryEffect: SideEffect,
) : State {

    override val currentExperience: Experience?
        get() = targetState.currentExperience
    override val currentStepIndex: Int?
        get() = targetState.currentStepIndex

    override fun take(action: Action): Transition? {
        return when (action) {
            is Retry -> next(targetState, retryEffect)
            // a new start means recovery never happened, move to Idling and continue with the new start attempt
            is StartExperience -> next(IdlingState, ContinuationEffect(action))
            // an explicit request to EndExperience just moves directly to IdlingState
            // can happen in ExperienceRender if a new higher priority experience is starting
            is EndExperience -> next(IdlingState)
            else -> null
        }
    }
}
