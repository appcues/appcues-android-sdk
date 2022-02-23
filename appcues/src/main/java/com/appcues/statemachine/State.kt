package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.di.AppcuesKoinComponent

internal interface State : AppcuesKoinComponent {
    data class Transition(val state: State, val continuation: Action? = null)

    val experience: Experience?

    fun handleAction(action: Action): Transition?
}
