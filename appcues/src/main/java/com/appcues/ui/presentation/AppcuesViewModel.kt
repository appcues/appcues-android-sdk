package com.appcues.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.analytics.SdkMetrics
import com.appcues.data.model.Experience
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepContainer
import com.appcues.data.model.StepReference.StepGroupPageIndex
import com.appcues.experiences.Experiences
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.StateMachine
import com.appcues.ui.StateMachineDirectory
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Idle
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class AppcuesViewModel(
    override val scope: Scope,
    private val renderContext: RenderContext,
    private val onDismiss: () -> Unit,
) : ViewModel(), KoinScopeComponent {

    sealed class UIState {
        object Idle : UIState()

        data class Rendering(
            val experience: Experience,
            val stepContainer: StepContainer,
            val position: Int,
            val flatStepIndex: Int,
            val metadata: Map<String, Any?>,
        ) : UIState()

        data class Dismissing(val continueAction: () -> Unit) : UIState()
    }

    private val appcuesCoroutineScope by inject<AppcuesCoroutineScope>()
    private val experiences by inject<Experiences>()
    private val stateMachineDirectory by inject<StateMachineDirectory>()
    private val actionProcessor by inject<ActionProcessor>()

    private val _uiState = MutableStateFlow<UIState>(Idle)

    val uiState: StateFlow<UIState>
        get() = _uiState

    init {
        stateMachineDirectory.getOwner(renderContext)?.run {
            stateMachine.collectStateFlow()
        }
    }

    private fun StateMachine.collectStateFlow() {
        viewModelScope.launch {
            stateFlow.collectLatest { result ->
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
                        // dismiss will trigger exit animations and remove experience UI
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

    fun onActivityChanged() {
        uiState.value.let { state ->
            // if current state IS Rendering this means that the Activity was removed
            // from an external source (ex deep link) and we should end the experience
            if (state is Rendering) {
                appcuesCoroutineScope.launch {
                    experiences.dismiss(renderContext, markComplete = false, destroyed = true)
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
                Rendering(this, stepContainers[containerId], stepIndexInContainer, flatStepIndex, metadata)
            } else null
        }
    }

    fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        actionProcessor.process(renderContext, actions, interactionType, viewDescription)
    }

    fun onPageChanged(index: Int) {
        uiState.value.let { state ->
            // if current state is Rendering but position is different than current
            // then we report new position to state machine
            if (state is Rendering && state.position != index) {
                appcuesCoroutineScope.launch {
                    experiences.show(renderContext, StepGroupPageIndex(index, state.flatStepIndex))
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

    fun dismiss() {
        onDismiss()
    }
}
