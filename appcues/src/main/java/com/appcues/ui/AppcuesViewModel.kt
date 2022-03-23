package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.StepContainer
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StepReference.StepIndex
import com.appcues.ui.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.AppcuesViewModel.UIState.Idle
import com.appcues.ui.AppcuesViewModel.UIState.Rendering
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class AppcuesViewModel(
    override val scope: Scope,
) : ViewModel(), KoinScopeComponent {

    sealed class UIState {
        object Idle : UIState()
        data class Rendering(val stepContainer: StepContainer, val position: Int) : UIState()
        object Dismissing : UIState()
    }

    val stateMachine by inject<StateMachine>()

    private val appcues by inject<Appcues>()

    private val _uiState = MutableStateFlow<UIState>(Idle)

    val uiState: StateFlow<UIState>
        get() = _uiState

    init {
        viewModelScope.launch {
            stateMachine.stateFlow.collectLatest { result ->
                when (result) {
                    is BeginningStep -> {
                        result.toRenderingState()?.run {
                            _uiState.value = this

                            // Notify state machine that we are rendering current step
                            viewModelScope.launch {
                                stateMachine.handleAction(RenderStep)
                            }
                        }
                    }
                    is EndingStep -> {
                        // the state tells us if we should dismiss the view (finish activity)
                        // either (A) completed last step of container or (B) close experience
                        // action was executed.

                        // dismiss will trigger exit animations and finish activity
                        if (result.dismiss) {
                            _uiState.value = Dismissing
                        }
                    }
                    // ignore other state changes
                    else -> Unit
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
                Rendering(stepContainers[containerId], stepIndexInContainer)
            } else null
        }
    }

    fun onEndExperience() {
        viewModelScope.launch {
            stateMachine.handleAction(EndExperience)
        }
    }

    fun onAction(experienceAction: ExperienceAction) {
        viewModelScope.launch {
            experienceAction.execute(appcues)
        }
    }

    fun onPageChanged(index: Int) {
        viewModelScope.launch {
            stateMachine.handleAction(StartStep(StepIndex(index)))
        }
    }
}
