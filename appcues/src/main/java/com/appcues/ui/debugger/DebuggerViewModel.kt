package com.appcues.ui.debugger

import androidx.lifecycle.ViewModel
import com.appcues.statemachine.StateMachine
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Creating
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dismissed
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dragging
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Idle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class DebuggerViewModel(
    override val scope: Scope,
) : ViewModel(), KoinScopeComponent {

    sealed class UIState {
        object Creating : UIState()
        object Idle : UIState()
        object Dragging : UIState()
        object Expanded : UIState()
        object Dismissing : UIState()
        object Dismissed : UIState()
    }

    val stateMachine by inject<StateMachine>()

    private val _uiState = MutableStateFlow<UIState>(Creating)

    val uiState: StateFlow<UIState>
        get() = _uiState

    fun onInit() {
        _uiState.value = Idle
    }

    fun onDismissAnimationCompleted() {
        if (_uiState.value is Dismissing) {
            _uiState.value = Dismissed
        }
    }

    fun onDragStart() {
        _uiState.value = Dragging
    }

    fun onDragEnd() {
        _uiState.value = Idle
    }

    fun onDismiss() {
        _uiState.value = Dismissing
    }

    fun onExpand() {
        _uiState.value = Expanded
    }
}
