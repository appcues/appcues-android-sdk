package com.appcues.statemachine

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceAlreadyActive
import com.appcues.statemachine.SideEffect.AwaitEffect
import com.appcues.statemachine.SideEffect.ContinuationEffect
import com.appcues.statemachine.SideEffect.PresentContainerEffect
import com.appcues.statemachine.SideEffect.ProcessActions
import com.appcues.statemachine.SideEffect.ReportErrorEffect
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StepReference.StepOffset
import com.appcues.statemachine.Transition.EmptyTransition
import com.appcues.statemachine.Transition.ErrorLoggingTransition
import com.appcues.statemachine.Transitions.Companion.fromRenderingStepToEndingExperience
import com.appcues.statemachine.Transitions.Companion.fromRenderingStepToEndingStep
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class StateMachine(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val config: AppcuesConfig,
    private val actionProcessor: ActionProcessor,
    private val lifecycleTracker: ExperienceLifecycleTracker,
    initialState: State = Idling
) {

    private val _stateFlow = MutableSharedFlow<State>(1)
    val stateFlow: SharedFlow<State>
        get() = _stateFlow

    private val _errorFlow = MutableSharedFlow<Error>(1)
    val errorFlow: SharedFlow<Error>
        get() = _errorFlow

    private var _state: State = initialState
    val state: State
        get() = _state

    private var mutex = Mutex()

    var onEndedExperience: ((Experience) -> Unit)? = null

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

        lifecycleTracker.start(this, { onEndedExperience?.invoke(it) })
    }

    fun stopLifecycleTracking() {
        lifecycleTracker.stop()
    }

    suspend fun handleAction(action: Action): ResultOf<State, Error> = mutex.withLock {
        return handleActionInternal(action)
    }

    private suspend fun handleActionInternal(action: Action): ResultOf<State, Error> {
        val transition = _state.take(action)
        val state = transition.state
        val sideEffect = transition.sideEffect

        if (state != null) {
            // update current state
            _state = state

            if (transition.emitStateChange) {
                // emit state change to all listeners via flow
                _stateFlow.emit(state)
            }
        }

        if (sideEffect != null) {
            return when (sideEffect) {
                is ContinuationEffect -> {
                    // recursive call on continuations to get the final return value
                    handleActionInternal(sideEffect.action)
                }

                is ReportErrorEffect -> {
                    _errorFlow.emit(sideEffect.error)
                    // return a failure if this call to `handleAction` ended with a reported error
                    Failure(sideEffect.error)
                }

                is PresentContainerEffect -> {
                    // this will execute the presentation, asynchronously, and return
                    // the subsequent action to take - either continue to Rendering, or report Error
                    handleActionInternal(sideEffect.presentContainer(actionProcessor))
                }

                is AwaitEffect -> {
                    sideEffect.completion.await()
                }

                is ProcessActions -> {
                    actionProcessor.process(sideEffect.actions)
                    Success(_state)
                }
            }
        } else {
            // if no side effect, return success with current state
            return Success(_state)
        }
    }

    private fun State.take(action: Action): Transition {
        return takeValidStateTransitions(this, action) ?: when (action) {
            // start experience action when experience is already active
            is StartExperience -> ErrorLoggingTransition(ExperienceAlreadyActive, false)
            // report error action
            is ReportError -> ErrorLoggingTransition(action.error, action.fatal)
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
            state is Idling && action is StartExperience ->
                state.fromIdlingToBeginningExperience(action)

            // BeginningExperience
            state is BeginningExperience && action is StartStep ->
                state.fromBeginningExperienceToBeginningStep(action)

            // BeginningStep
            state is BeginningStep && action is RenderStep ->
                state.fromBeginningStepToRenderingStep(action)

            // RenderingStep
            state is RenderingStep && action is StartStep ->
                determineStartStepTransition(state, action)

            state is RenderingStep && action is EndExperience ->
                state.fromRenderingStepToEndingExperience(action, appcuesCoroutineScope) { handleActionInternal(action) }

            // EndingStep
            state is EndingStep && action is EndExperience ->
                state.fromEndingStepToEndingExperience(action)

            state is EndingStep && action is StartStep ->
                state.fromEndingStepToBeginningStep(action)

            // EndingExperience
            state is EndingExperience && action is Reset ->
                state.fromEndingExperienceToIdling(action)

            // No valid combination of state plus action
            else -> null
        }
    }

    // helper to determine if StartStep should try to resolve and start the next step, or shortcut
    // to end the experience, if a continue action is executed while already on the last step
    private fun determineStartStepTransition(state: RenderingStep, action: StartStep): Transition {
        if (state.flatStepIndex == state.experience.flatSteps.size - 1) {
            // we are sitting on the last step if we get here - check if this is a continue with offset 1 (default continue)
            if (action.stepReference is StepOffset && action.stepReference.offset == 1) {
                // for a continue -> next, on the last step, treat this the same as a close
                val endExperience = EndExperience(markComplete = true, destroyed = false)
                return state.fromRenderingStepToEndingExperience(endExperience, appcuesCoroutineScope) {
                    handleActionInternal(endExperience)
                }
            }
        }

        // default behavior, attempt to start the requested step
        return state.fromRenderingStepToEndingStep(action, appcuesCoroutineScope) {
            handleActionInternal(StartStep(action.stepReference))
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
}
