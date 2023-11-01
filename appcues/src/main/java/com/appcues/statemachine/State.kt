package com.appcues.statemachine

import com.appcues.data.model.Experience

internal interface State {

    val currentExperience: Experience?

    val currentStepIndex: Int?

    fun take(action: Action): Transition?

    fun State.next(state: State, sideEffect: SideEffect? = null) = Transition(state, null, sideEffect)

    fun State.next(state: State, error: Error) = Transition(state, error, null)

    fun State.keep(error: Error? = null) = Transition(this, error, null)
}
