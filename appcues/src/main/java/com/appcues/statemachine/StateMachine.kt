package com.appcues.statemachine

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.data.model.Experience
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
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

internal class StateMachine(
    appcuesCoroutineScope: AppcuesCoroutineScope,
    private val config: AppcuesConfig,
) {

    private val _stateFlow = MutableSharedFlow<State>(1)
    val stateFlow: SharedFlow<State>
        get() = _stateFlow

    private val _errorFLow = MutableSharedFlow<Error>()
    val errorFLow: SharedFlow<Error>
        get() = _errorFLow

    // for internal convenience when one state transition depends on waiting for another transition
    // to complete "outside the system" - i.e. a UI driven update
    private val stateUpdateChannel = Channel<State>(capacity = 1, onBufferOverflow = DROP_OLDEST)

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

        appcuesCoroutineScope.launch {
            stateFlow.collect {
                stateUpdateChannel.send(it)
            }
        }
    }

    suspend fun handleAction(action: Action): ResultOf<State, Error> {
        val transition = _currentState.take(action)
        val state = transition.state
        val sideEffect = transition.sideEffect

        if (state != null) {
            // update current state
            _currentState = state

            // emit state change to all listeners via flow
            _stateFlow.emit(state)
        }

        if (sideEffect != null) {
            return when (sideEffect) {
                is ContinuationEffect -> {
                    // recursive call on continuations to get the final return value
                    handleAction(sideEffect.action)
                }
                is ReportErrorEffect -> {
                    _errorFLow.emit(sideEffect.error)
                    // return a failure if this call to `handleAction` ended with a reported error
                    Failure(sideEffect.error)
                }
                is PresentContainerEffect -> {
                    // kick off UI
                    sideEffect.experience.stepContainers[sideEffect.containerIndex].presentingTrait.present()
                    // wait on the RenderingStep state to flow in from the UI

                    var updatedState = stateUpdateChannel.receive()
                    while (updatedState.matching(RenderingStep::class, sideEffect.experience).not()) {
                        updatedState = stateUpdateChannel.receive()
                    }
                    // return the success for RenderingState - the resting state of machine
                    Success(updatedState)
                }
            }
        } else {
            // if no side effect, return success with current state
            return Success(_currentState)
        }
    }

    private fun <T : State> State.matching(clazz: KClass<T>, experience: Experience): Boolean {
        return clazz.isInstance(this) && this.experience?.instanceId == experience.instanceId
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
