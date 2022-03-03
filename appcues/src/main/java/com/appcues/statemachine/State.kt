package com.appcues.statemachine

import com.appcues.data.model.Experience

internal interface State {
    data class Transition(val state: State, val continuation: Action? = null)

    val experience: Experience?

    fun handleAction(action: Action): Transition?
}
