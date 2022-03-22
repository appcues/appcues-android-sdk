package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.StepContainer
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
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
                        // Notify state machine that we are good to render steps
                        viewModelScope.launch {
                            stateMachine.handleAction(RenderStep)
                        }
                    }
                    is RenderingStep -> {
                        result.experience.stepContainers.firstOrNull()?.let { container ->
                            // Render if there is a stepContainer
                            _uiState.value = Rendering(container, result.step)
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
}
