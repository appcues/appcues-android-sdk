package com.appcues.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.Appcues
import com.appcues.action.ActionRegistry
import com.appcues.data.model.Experience
import com.appcues.di.AppcuesKoinComponent
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.BeginningStep
import com.appcues.statemachine.states.EndingStep
import com.appcues.ui.AppcuesViewModel.UIState.Completed
import com.appcues.ui.AppcuesViewModel.UIState.Render
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.get
import org.koin.core.component.inject

internal class AppcuesViewModel(
    override val scopeId: String,
) : ViewModel(), AppcuesKoinComponent {

    sealed class UIState {
        data class Render(val experience: Experience) : UIState()
        object Completed : UIState()
    }

    private val stateMachine by inject<StateMachine>()

    private val _uiState = MutableLiveData<UIState>()

    val uiState: LiveData<UIState>
        get() = _uiState

    init {
        viewModelScope.launch {
            stateMachine.flow.collectLatest {
                when (it) {
                    is BeginningStep -> {
                        // this can happen multiple times in multi-step container
                        // and will trigger moving to another page forward/backward
                        _uiState.postValue(Render(it.experience))
                    }
                    is EndingStep -> {
                        // the state tells us if we should dismiss the view (finish activity)
                        // either (A) completed last step of container or (B) close experience
                        // action was executed.

                        if (it.dismissContainer) {
                            _uiState.postValue(Completed)
                        }
                    }
                }
            }
        }
    }

    fun onRender() {
        viewModelScope.launch {
            stateMachine.handleAction(RenderStep())
        }
    }

    fun onEndExperience() {
        viewModelScope.launch {
            stateMachine.handleAction(EndExperience())
        }
    }

    fun testAction() {
        // this simulates how the CloseAction would be invoked
        val actionRegistry = get<ActionRegistry>()
        val closeActionFactory = actionRegistry["@appcues/close"]
        val appcues = get<Appcues>()
        val closeAction = closeActionFactory?.invoke(hashMapOf())

        viewModelScope.launch {
            closeAction?.execute(appcues)
        }
    }
}
