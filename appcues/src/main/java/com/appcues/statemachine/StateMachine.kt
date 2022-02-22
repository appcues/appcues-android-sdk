package com.appcues.statemachine

import com.appcues.statemachine.states.Idling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class StateMachine(val scopeId: String) {
    private var _state: MutableStateFlow<State> = MutableStateFlow(Idling(scopeId))
    val state: StateFlow<State> = _state

    fun handleAction(action: Action) = _state.update { it.handleAction(action) }
}
