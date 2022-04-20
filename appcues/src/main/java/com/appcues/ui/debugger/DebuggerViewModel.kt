package com.appcues.ui.debugger

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.analytics.AnalyticsTracker
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Creating
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dismissed
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dragging
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Idle
import com.appcues.ui.debugger.EventType.SCREEN
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class DebuggerViewModel(
    override val scope: Scope,
) : ViewModel(), KoinScopeComponent {

    private val analyticsTracker by inject<AnalyticsTracker>()

    private val debuggerDataManager by inject<DebuggerDataManager>()

    sealed class UIState {
        object Creating : UIState()
        object Idle : UIState()
        data class Dragging(val dragAmount: Offset) : UIState()
        object Expanded : UIState()
        object Dismissing : UIState()
        object Dismissed : UIState()
    }

    private val _uiState = MutableStateFlow<UIState>(Creating)

    val uiState: StateFlow<UIState>
        get() = _uiState

    private val _statusInfo = MutableStateFlow<List<DebuggerStatusItem>>(arrayListOf())

    val statusInfo: StateFlow<List<DebuggerStatusItem>>
        get() = _statusInfo

    private val _events = MutableStateFlow<List<DebuggerEventItem>>(arrayListOf())

    val events: StateFlow<List<DebuggerEventItem>>
        get() = _events

    init {
        viewModelScope.launch {
            analyticsTracker.analyticsFlow.collect { activityRequest ->
                // dispatch to data manager so it can check for new experiences
                // and update status info if needed
                debuggerDataManager.onActivityRequest(activityRequest)

                activityRequest.events?.forEach {
                    _events.value = _events.value.plus(DebuggerEventItem(name = it.name, type = SCREEN))
                }
            }
        }

        viewModelScope.launch {
            debuggerDataManager.data.collect { items ->
                _statusInfo.value = items
            }
        }

        viewModelScope.launch {
            debuggerDataManager.start()
        }
    }

    fun onInit() {
        _uiState.value = Idle
    }

    fun onDismissAnimationCompleted() {
        if (_uiState.value is Dismissing) {
            _uiState.value = Dismissed
            viewModelScope.cancel()
        }
    }

    fun onDragging(dragAmount: Offset) {
        _uiState.value = Dragging(dragAmount)
    }

    fun onDragEnd() {
        _uiState.value = Idle
    }

    fun onDismiss() {
        _uiState.value = Dismissing
    }

    fun onFabClick() {
        if (_uiState.value == Idle) {
            _uiState.value = Expanded
        } else if (_uiState.value == Expanded) {
            _uiState.value = Idle
        }
    }

    fun onBackPress() {
        if (_uiState.value == Expanded) {
            _uiState.value = Idle
        }
    }

    fun onBackdropClick() {
        if (_uiState.value == Expanded) {
            _uiState.value = Idle
        }
    }

    fun onStatusTapAction(tapActionType: TapActionType) {
        viewModelScope.launch {
            debuggerDataManager.onTapAction(tapActionType)
        }
    }
}
