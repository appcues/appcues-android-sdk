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
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.StepReference.StepGroupPageIndex
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Idle
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering
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

        data class Dismissing(val continueAction: () -> Unit) : UIState()
    }

    private val _uiState = MutableStateFlow<UIState>(Idle)

    val uiState: StateFlow<UIState>
        get() = _uiState

    init {
        viewModelScope.launch {
            experienceRenderer.getStateFlow(renderContext)?.collectLatest { result ->
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
                coroutineScope.launch {
                    experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = true)
                }
            }
        }
    }

    private fun BeginningStep.toRenderingState(): Rendering? = with(experience) {
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
