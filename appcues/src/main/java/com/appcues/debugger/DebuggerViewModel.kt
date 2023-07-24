package com.appcues.debugger

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.AppcuesCoroutineScope
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.RenderContext
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebugMode.ScreenCapture
import com.appcues.debugger.DebuggerViewModel.ToastState.Rendering
import com.appcues.debugger.DebuggerViewModel.UIState.Creating
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissed
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.debugger.DebuggerViewModel.UIState.Idle
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.model.DebuggerFontItem
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.DebuggerToast
import com.appcues.debugger.model.DebuggerToast.ScreenCaptureFailure
import com.appcues.debugger.model.DebuggerToast.ScreenCaptureSuccess
import com.appcues.debugger.model.EventType
import com.appcues.debugger.model.TapActionType
import com.appcues.debugger.screencapture.Capture
import com.appcues.debugger.screencapture.ScreenCaptureProcessor
import com.appcues.debugger.ui.MutableDebuggerState
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val screenCaptureProcessor by inject<ScreenCaptureProcessor>()

    private val appcuesCoroutineScope by inject<AppcuesCoroutineScope>()

    private val experienceRenderer by inject<ExperienceRenderer>()

    sealed class UIState {
        object Creating : UIState()
        data class Idle(val mode: DebugMode) : UIState()
        data class Dragging(val dragAmount: Offset) : UIState()
        data class Expanded(val mode: DebugMode) : UIState()
        object Dismissing : UIState()
        object Dismissed : UIState()
    }

    sealed class ToastState {
        object Idle : ToastState()
        data class Rendering(val type: DebuggerToast) : ToastState()
    }

    private val _uiState = MutableStateFlow<UIState>(Creating)

    val uiState: StateFlow<UIState>
        get() = _uiState

    private val _toastState = MutableStateFlow<ToastState>(ToastState.Idle)

    val toastState: StateFlow<ToastState>
        get() = _toastState

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

    lateinit var mode: DebugMode

    init {
        with(viewModelScope) {
            launch {
                analyticsTracker.analyticsFlow.collect { trackingData ->
                    // dispatch to status manager so it can check for new experiences
                    // and update status info if needed
                    debuggerStatusManager.onActivityRequest(trackingData.request)
                    // dispatch to recent events manager so it stores all recent events and emits only
                    // what is set by the filter
                    debuggerRecentEventsManager.onTrackingData(trackingData)
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

    fun onStart(mode: DebugMode, reset: Boolean) {
        this.mode = mode

        when (mode) {
            is Debugger -> {
                val deepLinkPath = mode.deepLinkPath
                if (deepLinkPath.isNullOrEmpty()) {
                    // if no path is given, and currently expanded, go back to Idle
                    if (_uiState.value is Expanded ||
                        // OR if showing FAB but the mode changed, reset to new Idle mode
                        (_uiState.value is Idle && reset)
                    ) {
                        _uiState.value = Idle(mode)
                    }
                    return
                } else {
                    // the deep link might be from the debugger checking that the configuration is correct
                    // pass along to allow it to check
                    viewModelScope.launch {
                        debuggerStatusManager.checkDeepLinkValidation(deepLinkPath)
                    }
                }

                when (_uiState.value) {
                    // if currently idle and a new link comes in - expand to that link
                    is Idle -> _uiState.value = Expanded(mode)
                    // currently open - reset to the new path
                    is Expanded -> _uiState.value = Expanded(mode)
                    // otherwise, no valid link action available
                    else -> Unit
                }
            }
            is ScreenCapture -> {
                // if in debugger mode, expanded, go to new Idle state
                if (_uiState.value is Expanded ||
                    // OR if in Idle (FAB) mode but mode changed, reset to new Idle mode
                    (_uiState.value is Idle && reset)
                ) {
                    _uiState.value = Idle(mode)
                }
            }
        }
    }

    fun onRender() {
        when (val currentMode = mode) {
            is Debugger -> {
                val deepLinkPath = currentMode.deepLinkPath
                // if we had a deep link from initial startup, process it now
                _uiState.value = if (deepLinkPath.isNullOrEmpty()) Idle(currentMode) else Expanded(currentMode)
            }
            is ScreenCapture -> {
                _uiState.value = Idle(currentMode)
            }
        }
    }

    fun onDismissAnimationCompleted() {
        if (_uiState.value is Dismissing) {
            _uiState.value = Dismissed
            viewModelScope.cancel()
        }
    }

    fun transition(state: UIState) {
        _uiState.value = state
    }

    fun onFabClick() {
        when (val state = _uiState.value) {
            is Idle -> {
                _uiState.value = Expanded(state.mode)
            }
            is Expanded -> {
                _uiState.value = Idle(mode)
            }
            else -> Unit
        }
    }

    fun closeExpandedView() {
        if (_uiState.value is Expanded) {
            _uiState.value = Idle(mode)
        }
    }

    fun captureScreen(debuggerState: MutableDebuggerState) {
        appcuesCoroutineScope.launch {
            Log.i("Logcues", "capture screen dismiss")
            experienceRenderer.dismiss(RenderContext.Modal, markComplete = false, destroyed = false)

            withContext(Dispatchers.Main) {
                // capture screen
                val capture = screenCaptureProcessor.captureScreen()

                if (capture != null) {
                    debuggerState.screenCapture.value = capture
                } else {
                    // go back to idle if for some reason we could not capture the screen
                    closeExpandedView()
                }
            }
        }
    }

    fun onScreenCaptureConfirm(capture: Capture) {
        // return back to Idle for the current mode (ScreenCapture)
        _uiState.value = Idle(mode)

        when (val currentMode = mode) {
            // saving a capture is only valid in screen capture mode with token
            is ScreenCapture -> {
                viewModelScope.launch {
                    when (val result = screenCaptureProcessor.save(capture, currentMode.token)) {
                        is Success -> _toastState.value = Rendering(
                            type = ScreenCaptureSuccess(result.value) {
                                // on dismiss
                                _toastState.value = ToastState.Idle
                            }
                        )
                        is Failure -> _toastState.value = Rendering(
                            type = ScreenCaptureFailure(
                                capture = capture,
                                onDismiss = { _toastState.value = ToastState.Idle },
                                onRetry = {
                                    _toastState.value = ToastState.Idle
                                    onScreenCaptureConfirm(capture)
                                }
                            )
                        )
                    }
                }
            }
            else -> Unit
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

    fun reset() {
        viewModelScope.launch {
            debuggerStatusManager.reset()
            debuggerRecentEventsManager.reset()
        }
    }

    private fun List<DebuggerEventItem>.hideEventsForFab(): List<DebuggerEventItem> {
        return toMutableList().onEach { it.showOnFab = false }
    }
}
