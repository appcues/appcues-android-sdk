package com.appcues.debugger

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.analytics.AnalyticsTracker
import com.appcues.debugger.DebuggerViewModel.UIState.Creating
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissed
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.debugger.DebuggerViewModel.UIState.Dragging
import com.appcues.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.debugger.DebuggerViewModel.UIState.Idle
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.model.DebuggerFontItem
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.EventType
import com.appcues.debugger.model.TapActionType
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class DebuggerViewModel(
    override val scope: Scope,
) : ViewModel(), KoinScopeComponent {

    private val analyticsTracker by inject<AnalyticsTracker>()

    private val debuggerStatusManager by inject<DebuggerStatusManager>()

    private val debuggerRecentEventsManager by inject<DebuggerRecentEventsManager>()

    private val debuggerFontManager by inject<DebuggerFontManager>()

    sealed class UIState {
        object Creating : UIState()
        object Idle : UIState()
        data class Dragging(val dragAmount: Offset) : UIState()
        data class Expanded(val deeplinkPath: String? = null) : UIState()
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

    private val _currentFilter = MutableStateFlow<EventType?>(null)

    val currentFilter: StateFlow<EventType?>
        get() = _currentFilter

    val appSpecificFonts: List<DebuggerFontItem>
        get() = debuggerFontManager.getAppSpecificFonts()

    val systemFonts: List<DebuggerFontItem>
        get() = debuggerFontManager.getSystemFonts()

    val allFonts: List<DebuggerFontItem>
        get() = debuggerFontManager.getAllFonts()

    private var deeplinkPath: String? = null

    init {
        with(viewModelScope) {
            launch {
                analyticsTracker.analyticsFlow.collect { trackingData ->
                    // dispatch to status manager so it can check for new experiences
                    // and update status info if needed
                    debuggerStatusManager.onActivityRequest(trackingData.request)
                    // dispatch to recent events manager so it stores all recent events and emits only
                    // what is set by the filter
                    debuggerRecentEventsManager.onActivityRequest(trackingData.request)
                }
            }

            launch {
                debuggerStatusManager.data.collect { items ->
                    _statusInfo.value = items
                }
            }

            launch {
                debuggerRecentEventsManager.data.collectIndexed { index, value ->
                    // every time we start collecting we want to not show the elements that we take
                    _events.value = if (index == 0) value.hideEventsForFab() else value
                }
            }
            launch {
                debuggerStatusManager.start()
            }
        }
    }

    fun onStart(deeplinkPath: String?) {
        if (deeplinkPath.isNullOrEmpty()) {
            // if no path is given, and currently expanded, go back to idle
            if (_uiState.value is Expanded) {
                _uiState.value = Idle
            }
            return
        }

        when (_uiState.value) {
            // if still in initial start of debugger - save the path for onRender to use
            Creating -> this.deeplinkPath = deeplinkPath
            // if currently idle and a new link comes in - expand to that link
            Idle -> _uiState.value = Expanded(deeplinkPath)
            // currently open - reset to the new path
            is Expanded -> _uiState.value = Expanded(deeplinkPath)
            // otherwise, no valid link action available
            else -> Unit
        }
    }

    fun onRender() {
        // if we had a deeplink from initial startup, process it now
        _uiState.value = if (deeplinkPath.isNullOrEmpty()) Idle else Expanded(deeplinkPath)
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
            _uiState.value = Expanded()
        } else if (_uiState.value is Expanded) {
            _uiState.value = Idle
        }
    }

    fun onBackPress() {
        if (_uiState.value is Expanded) {
            _uiState.value = Idle
        }
    }

    fun onBackdropClick() {
        if (_uiState.value is Expanded) {
            _uiState.value = Idle
        }
    }

    fun onStatusTapAction(tapActionType: TapActionType) {
        viewModelScope.launch {
            debuggerStatusManager.onTapAction(tapActionType)
        }
    }

    fun onApplyEventFilter(eventType: EventType?) {
        viewModelScope.launch {
            _currentFilter.emit(eventType)
            debuggerRecentEventsManager.onApplyEventFilter(eventType)
        }
    }

    fun onDisplayedEventTimeout() {
        viewModelScope.launch {
            _events.value = _events.value.hideEventsForFab()
        }
    }

    private fun List<DebuggerEventItem>.hideEventsForFab(): List<DebuggerEventItem> {
        return toMutableList().onEach { it.showOnFab = false }
    }

    fun onDetailDismiss() {
        // reset the view state to remove any deeplink path
        _uiState.value = Expanded()
    }
}
