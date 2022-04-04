package com.appcues.statemachine

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceAlreadyActive
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.Transition.EmptyTransition
import com.appcues.statemachine.Transition.ErrorLoggingTransition
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

internal typealias StateResult = ResultOf<State, Error>

internal class StateMachine(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val config: AppcuesConfig,
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

    init {
        appcuesCoroutineScope.launch {
            stateFlow.collect {
                when (it) {
                    is BeginningExperience -> config.experienceListener?.experienceStarted(it.experience.id)
                    is EndingExperience -> config.experienceListener?.experienceFinished(it.experience.id)
                    else -> Unit
                }
            }
        }
    }

    fun handleAction(action: Action) {
        appcuesCoroutineScope.launch {

            // current state will take action and apply transition
            _currentState.take(action).applyTransition(this@StateMachine) {
                // update current state
                _currentState = it

                // emit state change to all listeners via flow
                _stateResultFlow.emit(Success(it))
            }
        }
    }

    suspend fun reportError(error: Error) {
        _stateResultFlow.emit(Failure(error))
    }

    private fun State.take(action: Action): Transition {
        return takeValidStateTransitions(this, action) ?: when (action) {
            // start experience action when experience is already active
            is StartExperience -> ErrorLoggingTransition(ExperienceAlreadyActive(action.experience, "Experience already active"))
            // report error action
            is ReportError -> ErrorLoggingTransition(action.error)
            // undefined transition - no-op
            else -> EmptyTransition()
        }
    }

    /**
     * returns Transition for a valid combination of state plus action or null
     */
    private fun takeValidStateTransitions(state: State, action: Action) = with(Transitions) {
        when {
            // Idling
            state is Idling && action is StartExperience -> state.fromIdlingToBeginningExperience(action)
            // BeginningExperience
            state is BeginningExperience && action is StartStep -> state.fromBeginningExperienceToBeginningStep(action)
            // BeginningStep
            state is BeginningStep && action is RenderStep -> state.fromBeginningStepToRenderingStep(action)
            // RenderingStep
            state is RenderingStep && action is StartStep -> state.fromRenderingStepToEndingStep(action)
            state is RenderingStep && action is EndExperience -> state.fromRenderingStepToEndingExperience(action)
            // EndingStep
            state is EndingStep && action is EndExperience -> state.fromEndingStepToEndingExperience(action)
            state is EndingStep && action is StartStep -> state.fromEndingStepToBeginningStep(action)
            // EndingExperience
            state is EndingExperience && action is Reset -> state.fromEndingExperienceToIdling(action)
            // No valid combination of state plus action
            else -> null
        }
    }
}
