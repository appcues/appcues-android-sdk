package com.appcues.statemachine

import com.appcues.action.ActionProcessor
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Error.ExperienceAlreadyActive
import com.appcues.statemachine.states.IdlingState
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class StateMachine(
    private val actionProcessor: ActionProcessor,
    private val lifecycleTracker: ExperienceLifecycleTracker,
    initialState: State = IdlingState,
    private val onEndedExperience: (Experience) -> Unit = {},
) {

    private val _stateFlow = MutableSharedFlow<State>(1)
    val stateFlow: Flow<State>
        // important that we use .distinctUntilChanged explicitly here, as it is not enabled
        // by default on SharedFlows (only on StateFlows) - duplicate states can sent
        // during error transitions, and we don't want those to actually be emitted to listeners
        get() = _stateFlow.distinctUntilChanged()

    private val _errorFlow = MutableSharedFlow<Error>()
    val errorFlow: Flow<Error>
        // don't need .distinctUntilChanged() here since it would not be expected
        // to send the same error through multiple times
        get() = _errorFlow

    private var _state: State = initialState
    val state: State
        get() = _state

    private val mutex = Mutex()
    suspend fun handleAction(action: Action): ResultOf<State, Error> = mutex.withLock {
        handleActionInternal(action)
    }

    private suspend fun handleActionInternal(action: Action): ResultOf<State, Error> {
        // if we are in idling state and get incoming StartExperience
        // we should start tracking events
        if (_state is IdlingState && action is StartExperience) {
            startLifecycleTracking()
        }

        with(_state take action) {
            // propagate new state
            // duplicate state will be ignored by flow used externally
            // due to the usage of .distinctUntilChanged()
            _state = newState
            _stateFlow.emit(newState)

            // if an error to report, emit and return failure
            if (error != null) {
                _errorFlow.emit(error)
                return Failure(error)
            }

            val next = sideEffect?.launch(actionProcessor)

            if (next != null) {
                return handleActionInternal(next)
            }

            return Success(newState).also {
                // after processing the action on current state, if we get Idling
                // we should stop tracking events
                if (it.value is IdlingState) {
                    stopLifecycleTracking()
                }
            }
        }
    }

    suspend fun stop(dismiss: Boolean) {
        handleAction(
            EndExperience(
                markComplete = false,
                // destroyed means the UI was already dismissed, so invert the dismiss direction passed in
                // for this use case - when !destroyed, the state machine will wait on the UI to dismiss and
                // signal to move to next step
                destroyed = !dismiss,
                // special case - no complete/dismiss analytics tracked on a force stop
                trackAnalytics = false
            )
        )
    }

    private infix fun State.take(action: Action): Transition {
        return take(action) ?: when (action) {
            // start experience action when experience is already active
            is StartExperience -> keep(ExperienceAlreadyActive)
            // report error action
            is ReportError -> if (action.fatal) exit(action.error) else keep(action.error)
            // undefined transition - no-op
            else -> keep()
        }
    }

    private fun startLifecycleTracking() {
        lifecycleTracker.start(this, onEndedExperience)
    }

    private fun stopLifecycleTracking() {
        lifecycleTracker.stop()
    }
}
