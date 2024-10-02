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
import com.appcues.statemachine.effects.AwaitDismissEffect
import com.appcues.statemachine.states.EndingStepState
import com.appcues.statemachine.states.RenderingStepState
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Idle
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class AppcuesViewModel(
    private val renderContext: RenderContext,
    private val coroutineScope: AppcuesCoroutineScope,
    private val experienceRenderer: ExperienceRenderer,
    private val actionProcessor: ActionProcessor,
    private val onDismiss: () -> Unit,
) : ViewModel() {

    sealed class UIState {
        object Idle : UIState()

        data class Rendering(
            val experience: Experience,
            val stepContainer: StepContainer,
            val position: Int,
            val flatStepIndex: Int,
            val metadata: Map<String, Any?>,
        ) : UIState()

        data class Dismissing(val awaitDismissEffect: AwaitDismissEffect) : UIState()
    }

    private val _uiState = MutableStateFlow<UIState>(Idle)

    val uiState: StateFlow<UIState>
        get() = _uiState

    private val statesJob: Job?

    init {
        statesJob = collectStates()

        if (statesJob == null) onDismiss()
    }

    private fun collectStates(): Job? {
        val stateFlow = experienceRenderer.getStateFlow(renderContext) ?: return null
        return viewModelScope.launch {
            stateFlow.collectLatest { state ->
                if (state is RenderingStepState) {
                    state.toRenderingState()?.let {
                        SdkMetrics.renderStart(state.experience.requestId)
                        _uiState.value = it
                    }
                } else if (state is EndingStepState && state.awaitDismissEffect != null) {
                    // since we are dismissing we don't need to collect states anymore
                    statesJob?.cancel()
                    // dismiss will trigger exit animations and remove experience UI
                    _uiState.value = Dismissing(state.awaitDismissEffect)
                }
            }
        }
    }

    fun onActivityChanged() {
        uiState.value.let { state ->
            // if current state IS Rendering this means that the Activity was removed
            // from an external source (ex deep link) and we should end the experience
            if (state is Rendering) {
                coroutineScope.launch {
                    experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = true)
                }
            }
        }
    }

    private fun RenderingStepState.toRenderingState(): Rendering? = with(experience) {
        // find the container index
        val containerId = groupLookup[flatStepIndex] ?: return null
        // find the step index in relation to the container
        val stepIndexInContainer = stepIndexLookup[flatStepIndex] ?: return null
        // returns rendering state
        return Rendering(
            experience = this,
            stepContainer = stepContainers[containerId],
            position = stepIndexInContainer,
            flatStepIndex = flatStepIndex,
            metadata = metadata
        )
    }

    fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        actionProcessor.process(renderContext, actions, interactionType, viewDescription)
    }

    fun onPageChanged(index: Int) {
        uiState.value.let { state ->
            // if current state is Rendering but position is different than current
            // then we report new position to state machine
            if (state is Rendering && state.position != index) {
                coroutineScope.launch {
                    experienceRenderer.show(renderContext, StepGroupPageIndex(index, state.flatStepIndex))
                }
            }
        }
    }

    fun onDismissed(awaitDismissEffect: AwaitDismissEffect) {
        onDismiss()
        awaitDismissEffect.dismissed()
    }

    fun canDismiss(): Boolean {
        val state = uiState.value
        return state is Rendering && state.experience.allowDismissal(state.flatStepIndex)
    }

    fun requestDismissal() {
        val state = uiState.value
        if (state is Rendering && state.experience.allowDismissal(state.flatStepIndex)) {
            coroutineScope.launch {
                experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = false)
            }
        }
    }

    fun onConfigurationChanged() {
        coroutineScope.launch {
            experienceRenderer.onViewConfigurationChanged(renderContext)
        }
    }
}
