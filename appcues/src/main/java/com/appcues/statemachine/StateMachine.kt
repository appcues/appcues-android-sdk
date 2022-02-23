package com.appcues.statemachine

import com.appcues.statemachine.states.Idling
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class StateMachine(scopeId: String) {
    private var _flow = MutableSharedFlow<State>(1)
    val flow = _flow.asSharedFlow()

    private var _currentState: State = Idling(scopeId)

    suspend fun handleAction(action: Action) {
        _currentState.handleAction(action)?.also { change ->
            // update current state
            _currentState = change.state

            // emit state change to all listeners via flow
            _flow.emit(change.state)

            // if there is a continuation action (i.e. auto-transition), recurse
            val nextAction = change.continuation
            if (nextAction != null) {
                handleAction(nextAction)
            }
        }
    }
}
