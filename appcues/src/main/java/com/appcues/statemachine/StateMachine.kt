package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.statemachine.states.Idling
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class StateMachine(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) {
    private var _stateFlow = MutableSharedFlow<State>(1)
    val stateFlow = _stateFlow.asSharedFlow()

    private var _errorFlow = MutableSharedFlow<Error>()
    val errorFlow = _errorFlow.asSharedFlow()

    private var _currentState: State = Idling()

    fun handleAction(action: Action) {
        appcuesCoroutineScope.launch {
            _currentState.handleAction(action)?.also { change ->

                change.state?.let {
                    // update current state
                    _currentState = it

                    // emit state change to all listeners via flow
                    _stateFlow.emit(it)
                }

                change.continuation?.let {
                    // if there is a continuation action (i.e. auto-transition), recurse
                    handleAction(it)
                }

                change.error?.let {
                    // if some error occurred, propagate it to observers
                    _errorFlow.emit(it)
                }
            }
        }
    }
}
