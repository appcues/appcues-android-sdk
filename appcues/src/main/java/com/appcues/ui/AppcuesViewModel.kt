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
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StateResult.Failure
import com.appcues.statemachine.StateResult.Success
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
            stateMachine.stateResultFlow.collectLatest { result ->
                when (result) {
                    is Success -> with(result.state) {
                        when (this) {
                            is BeginningStep -> {
                                // this can happen multiple times in multi-step container
                                // and will trigger moving to another page forward/backward
                                experience.stepContainer.firstOrNull()?.let { container ->
                                    // Render if there is a stepContainer
                                    _uiState.value = Rendering(container, step)
                                    // Notify state machine that we will render step
                                    viewModelScope.launch {
                                        stateMachine.handleAction(RenderStep())
                                    }
                                }
                            }
                            is EndingStep -> {
                                // the state tells us if we should dismiss the view (finish activity)
                                // either (A) completed last step of container or (B) close experience
                                // action was executed.

                                // dismiss will trigger exit animations and finish activity
                                _uiState.value = if (dismiss) Dismissing else _uiState.value
                            }
                            else -> { /* no action on other state changes */ }
                        }
                    }
                    is Failure -> { /* no action on failure cases */ }
                }
            }
        }
    }

    fun onEndExperience() {
        viewModelScope.launch {
            stateMachine.handleAction(EndExperience())
        }
    }

    fun onAction(experienceAction: ExperienceAction) {
        viewModelScope.launch {
            experienceAction.execute(appcues)
        }
    }
}
