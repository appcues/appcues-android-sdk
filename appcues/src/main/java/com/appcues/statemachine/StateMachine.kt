package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.StateResult.Success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class StateMachine(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) {
    var stateResultFlow = MutableSharedFlow<StateResult>(1)

    private var _currentState: State = Idling()

    fun handleAction(action: Action) {
        appcuesCoroutineScope.launch {
            _currentState.transition(action)?.also { transition ->

                transition.state?.let {
                    // update current state
                    _currentState = it

                    // emit state change to all listeners via flow
                    stateResultFlow.emit(Success(it))
                }

                transition.sideEffect?.execute(this@StateMachine)
            }
        }
    }
}
