package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.statemachine.State.Idling
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
            _currentState.transition(action)?.also { transition ->

                transition.state?.let {
                    // update current state
                    _currentState = it

                    // emit state change to all listeners via flow
                    _stateFlow.emit(it)
                }

                transition.sideEffect?.let {
                    it.execute(this@StateMachine)
                }
            }
        }
    }

    suspend fun handleError(error: Error) = _errorFlow.emit(error)
}
