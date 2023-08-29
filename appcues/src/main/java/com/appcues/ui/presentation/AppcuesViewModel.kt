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
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.StepReference.StepGroupPageIndex
import com.appcues.trait.AppcuesTraitException
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Idle
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class AppcuesViewModel(
    override val scope: Scope,
    private val renderContext: RenderContext,
    private val onDismiss: () -> Unit,
    private val setViewVisible: (Boolean) -> Unit,
) : ViewModel(), KoinScopeComponent {

    sealed class UIState {
        object Idle : UIState()

        data class Rendering(
            val experience: Experience,
            val stepContainer: StepContainer,
            val position: Int,
            val flatStepIndex: Int,
            val isPreview: Boolean,
            private val completionHandler: ((ResultOf<Unit, com.appcues.statemachine.Error>) -> Unit),
        ) : UIState() {
            // used to track presentation state, to avoid calling the completion multiple times
            private var presented: Boolean = false
            private val mutex = Mutex()

            // returns true if the completion handler was invoked, false if not (already presented)
            suspend fun presentationComplete(result: ResultOf<Unit, com.appcues.statemachine.Error>): Boolean = mutex.withLock {
                // this is to guard against the completionHandler being called multiple times, which
                // could happen in the rare edge case of the Activity being removed out from under
                // the experience.
                //
                // If this happens, post-render - it will flow through the normal dismiss, and
                // this handler will not be called (since already presented).
                //
                // If this happens pre-render, it will trigger an error, flowing through this handler
                // to notify the state machine that the render never completed.
                if (!presented) {
                    presented = true
                    completionHandler(result)
                    return true
                } else {
                    return false
                }
            }
        }

        data class Dismissing(val continueAction: () -> Unit) : UIState()
    }

    private val appcuesCoroutineScope by inject<AppcuesCoroutineScope>()
    private val experienceRenderer by inject<ExperienceRenderer>()
    private val actionProcessor by inject<ActionProcessor>()

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

    fun onPresentationComplete() {
        uiState.value.let { state ->
            if (state is Rendering) {
                appcuesCoroutineScope.launch {
                    state.presentationComplete(Success(Unit))
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
                    // this is to avoid any state machine hang waiting for this callback, if, the experience
                    // was destroyed before presentation completed. Normally, the presentation complete callback
                    // will have already been triggered during first render, and this will do nothing.
                    val handled = state.presentationComplete(
                        Failure(
                            StepError(
                                experience = state.experience,
                                stepIndex = state.flatStepIndex,
                                message = "Activity changed during render of step ${state.flatStepIndex}",
                            )
                        )
                    )

                    // this means that the failure above was not processed, since the presentation had already
                    // completed. In this case, all we need to do is dismiss the experience as destroyed.
                    if (!handled) {
                        experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = true)
                    }
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
        actionProcessor.process(renderContext, actions, interactionType, viewDescription)
    }

    fun onPageChanged(index: Int) {
        uiState.value.let { state ->
            // if current state is Rendering but position is different than current
            // then we report new position to state machine
            if (state is Rendering && state.position != index) {
                appcuesCoroutineScope.launch {
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

    fun updateViewVisibility(isVisible: Boolean) {
        setViewVisible(isVisible)
    }

    fun onTraitException(exception: AppcuesTraitException) {
        appcuesCoroutineScope.launch {
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
    }

    fun dismiss() {
        onDismiss()
    }
}
