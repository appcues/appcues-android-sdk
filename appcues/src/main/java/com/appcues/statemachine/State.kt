package com.appcues.statemachine

import com.appcues.data.model.Experience

internal interface State {
    val experience: Experience?

    fun handleAction(action: Action): Transition?
}
