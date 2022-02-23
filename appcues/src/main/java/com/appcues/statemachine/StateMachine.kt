package com.appcues.statemachine

import com.appcues.logging.Logcues
import com.appcues.statemachine.states.Idling
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class StateMachine(
    scopeId: String,
    private val logger: Logcues
) : CoroutineScope {
    private val parentJob = SupervisorJob()

    override val coroutineContext = parentJob + Dispatchers.Main + CoroutineExceptionHandler { _, error ->
        logger.info("StateMachine error handler -> exception: $error")
    }

    private var _flow = MutableSharedFlow<State>(1)
    val flow = _flow.asSharedFlow()

    private var _currentState: State = Idling(scopeId)

    init {
        launch {
            flow.collect {
                logger.info("moved to state $it")
            }
        }
    }

    fun handleAction(action: Action) {
        launch {
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
