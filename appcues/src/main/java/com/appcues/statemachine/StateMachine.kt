package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.statemachine.states.Idling
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class StateMachine(
    private val appcuesCoroutineScope: AppcuesCoroutineScope
) {
    private var _flow = MutableSharedFlow<State>(1)
    val flow = _flow.asSharedFlow()

    private var _currentState: State = Idling()

    fun handleAction(action: Action) {
        appcuesCoroutineScope.launch {
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
}
