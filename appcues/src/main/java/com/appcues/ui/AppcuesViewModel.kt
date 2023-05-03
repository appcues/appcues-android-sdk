package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.analytics.SdkMetrics
import com.appcues.data.model.Experience
import com.appcues.data.model.StepContainer
import com.appcues.statemachine.Action.Pause
import com.appcues.statemachine.Action.Resume
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StepReference.StepGroupPageIndex
import com.appcues.trait.AppcuesTraitException
import com.appcues.ui.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.AppcuesViewModel.UIState.Idle
import com.appcues.ui.AppcuesViewModel.UIState.Rendering
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class AppcuesViewModel(
    override val scope: Scope,
    private val onDismiss: () -> Unit,
) : ViewModel(), KoinScopeComponent {

    sealed class UIState {
        object Idle : UIState()

        data class Rendering(
            val experience: Experience,
            val stepContainer: StepContainer,
            val position: Int,
            val flatStepIndex: Int,
            val isPreview: Boolean,
            val presentationComplete: ((ResultOf<Unit, com.appcues.statemachine.Error>) -> Unit),
        ) : UIState()

        data class Dismissing(val continueAction: () -> Unit) : UIState()
    }

    private val stateMachine by inject<StateMachine>()
    private val actionProcessor by inject<ActionProcessor>()
    private val appcuesCoroutineScope by inject<AppcuesCoroutineScope>()
    private val experienceRenderer by inject<ExperienceRenderer>()

    private val _uiState = MutableStateFlow<UIState>(Idle)

    val uiState: StateFlow<UIState>
        get() = _uiState

    init {
        viewModelScope.launch {
            stateMachine.stateFlow.collectLatest { result ->
                // don't collect if we are Dismissing
                if (uiState.value is Dismissing) return@collectLatest

                when (result) {
                    is BeginningStep -> {
                        result.toRenderingState()?.let {
                            SdkMetrics.renderStart(result.experience.requestId)
                            _uiState.value = it
                        }
                    }
                    is EndingStep -> {
                        // dismiss will trigger exit animations and finish activity
                        if (result.dismissAndContinue != null) {
                            _uiState.value = Dismissing(result.dismissAndContinue)
                        }
                    }
                    // ignore other state changes
                    else -> Unit
                }
            }
        }
    }

    // handling the special case where the hosting activity is removed by the OS from outside
    // normal experience interactions (i.e. a deep link)
    override fun onCleared() {
        super.onCleared()

        uiState.value.let { state ->
            // if current state IS Rendering this means that the Activity was removed
            // from an external source (ex deep link) and we should end the experience
            if (state is Rendering) {
                appcuesCoroutineScope.launch {
                    experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = true)
                }
            }
        }
    }

    private fun BeginningStep.toRenderingState(): Rendering? {
        return with(experience) {
            // find the container index
            val containerId = groupLookup[flatStepIndex]
            // find the step index in relation to the container
            val stepIndexInContainer = stepIndexLookup[flatStepIndex]
            // if both are valid ids we return Rendering else null
            if (containerId != null && stepIndexInContainer != null) {
                // returns rendering state
                Rendering(this, stepContainers[containerId], stepIndexInContainer, flatStepIndex, published.not(), presentationComplete)
            } else null
        }
    }

    fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        actionProcessor.process(actions, interactionType, viewDescription)
    }

    fun onPageChanged(index: Int) {
        uiState.value.let { state ->
            // if current state is Rendering but position is different than current
            // then we report new position to state machine
            if (state is Rendering && state.position != index) {
                appcuesCoroutineScope.launch {
                    stateMachine.handleAction(StartStep(StepGroupPageIndex(index, state.flatStepIndex)))
                }
            }
        }
    }

    fun onBackPressed() {
        uiState.value.let { state ->
            // if current state IS Rendering then we process the action
            if (state is Rendering) {
                appcuesCoroutineScope.launch {
                    experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)
                }
            }
        }
    }

    fun onFinish() {
        uiState.value.let { state ->
            // if current state IS dismissing we send the continueAction from EndingStep
            if (state is Dismissing) {
                state.continueAction()
            }
        }
    }

    fun onTraitException(exception: AppcuesTraitException) {
        uiState.value.let { state ->
            // if currently attempting to Render and failed, signal back
            // the render complete (even on fail) and then continue to report
            // error and reset
            if (state is Rendering) {
                state.presentationComplete(
                    Failure(
                        StepError(
                            experience = state.experience,
                            stepIndex = state.flatStepIndex,
                            // this message cannot be null in AppcuesTraitException, but the parent Exception class
                            // has it optional. Thus, this fallback message should never actually be used.
                            message = exception.message ?: "Unable to render step ${state.flatStepIndex}",
                        )
                    )
                )
            }
        }

        // dismiss this experience
        dismiss()
    }

    fun dismiss() {
        onDismiss()
    }

    fun onPause() {
        if (uiState.value is Dismissing) return

        viewModelScope.launch {
            stateMachine.handleAction(Pause)
        }
    }

    fun onResume() {
        viewModelScope.launch {
            stateMachine.handleAction(Resume)
        }
    }

    fun refreshPreview() {
        uiState.value.let { state ->
            // if current state IS Rendering and we are Previewing then we refresh
            if (state is Rendering && state.isPreview) {
                appcuesCoroutineScope.launch {
                    experienceRenderer.preview(state.experience.id.toString())
                }
            }
        }
    }
}
