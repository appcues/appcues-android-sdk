package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.statemachine.states.IdlingState

internal interface State {

    val currentExperience: Experience?

    val currentStepIndex: Int?

    fun take(action: Action): Transition?

    fun State.next(state: State, sideEffect: SideEffect? = null) = Transition(state, null, sideEffect)

    fun State.keep(error: Error? = null) = Transition(this, error, null)

    fun State.exit(error: Error) = Transition(IdlingState, error, null)
}
