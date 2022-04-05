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
import com.appcues.statemachine.SideEffect.ContinuationEffect
import com.appcues.statemachine.SideEffect.PresentContainerEffect
import com.appcues.statemachine.SideEffect.ReportErrorEffect
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.Transition.EmptyTransition
import com.appcues.statemachine.Transition.ErrorLoggingTransition
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class StateMachine(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val config: AppcuesConfig,
) {

    private var _stateFlow = MutableSharedFlow<State>(1)
    val stateFlow: SharedFlow<State>
        get() = _stateFlow

    private var _errorFLow = MutableSharedFlow<Error>()
    val errorFLow: SharedFlow<Error>
        get() = _errorFLow

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
            apply(action)
        }
    }

    private suspend fun apply(action: Action) {
        val transition = _currentState.take(action)

        transition.state?.let {
            // update current state
            _currentState = it

            // emit state change to all listeners via flow
            _stateFlow.emit(it)
        }

        transition.sideEffect?.let {
            when (it) {
                is ContinuationEffect -> apply(it.action)
                is ReportErrorEffect -> _errorFLow.emit(it.error)
                is PresentContainerEffect -> it.experience.stepContainers[it.containerIndex].presentingTrait.present()
            }
        }
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
