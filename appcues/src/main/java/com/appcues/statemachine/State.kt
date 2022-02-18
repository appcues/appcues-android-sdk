package com.appcues.statemachine

internal interface State {
    fun handleAction(action: Action): State
}
