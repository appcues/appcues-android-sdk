package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.statemachine.State.Idling
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

internal typealias StateResult = ResultOf<State, Error>

internal class StateMachine(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) {

    private var _stateResultFlow = MutableSharedFlow<StateResult>(1)
    val stateResultFlow: SharedFlow<StateResult>
        get() = _stateResultFlow

    // this provides a way to observer a flow that is only passing along
    // state updates (no error cases)
    val stateFlow: SharedFlow<State>
        get() = stateResultFlow
            .filterIsInstance<Success<State>>()
            .map { it.value }
            .shareIn(appcuesCoroutineScope, SharingStarted.Lazily, 1)

    private var _currentState: State = Idling()

    fun handleAction(action: Action) {
        appcuesCoroutineScope.launch {
            _currentState.transition(action)?.also { transition ->

                transition.state?.let {
                    // update current state
                    _currentState = it

                    // emit state change to all listeners via flow
                    _stateResultFlow.emit(Success(it))
                }

                transition.sideEffect?.execute(this@StateMachine)
            }
        }
    }

    suspend fun reportError(error: Error) = _stateResultFlow.emit(Failure(error))
}
