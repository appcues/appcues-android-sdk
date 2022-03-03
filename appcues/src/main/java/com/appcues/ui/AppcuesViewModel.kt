package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.StepContainer
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.BeginningStep
import com.appcues.statemachine.states.EndingStep
import com.appcues.ui.AppcuesViewModel.UIAction.Finish
import com.appcues.ui.AppcuesViewModel.UIState.Idle
import com.appcues.ui.AppcuesViewModel.UIState.Render
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
        data class Render(val stepContainer: StepContainer, val position: Int) : UIState()
    }

    sealed class UIAction {
        object Finish : UIAction()
    }

    val stateMachine by inject<StateMachine>()

    private val appcues by inject<Appcues>()

    private val _uiState = MutableStateFlow<UIState>(Idle)

    val uiState: StateFlow<UIState>
        get() = _uiState

    private val _uiAction = MutableSharedFlow<UIAction>(replay = 0)

    val uiAction: SharedFlow<UIAction>
        get() = _uiAction

    init {
        viewModelScope.launch {
            stateMachine.flow.collectLatest {
                when (it) {
                    is BeginningStep -> {
                        // this can happen multiple times in multi-step container
                        // and will trigger moving to another page forward/backward
                        it.experience.stepContainer.firstOrNull()?.let { container ->
                            // Render if there is a stepContainer
                            _uiState.value = Render(container, it.step)
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

                        if (it.dismissContainer) {
                            _uiAction.emit(Finish)
                        }
                    }
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
