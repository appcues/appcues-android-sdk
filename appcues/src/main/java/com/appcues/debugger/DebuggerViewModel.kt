package com.appcues.debugger

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.AppcuesCoroutineScope
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
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import com.appcues.logging.LogMessage
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DebuggerViewModel(override val scope: AppcuesScope, debugMode: DebugMode) : ViewModel(), AppcuesComponent {

    private val debuggerStatusManager by inject<DebuggerStatusManager>()

    private val debuggerRecentEventsManager by inject<DebuggerRecentEventsManager>()

    private val debuggerFontManager by inject<DebuggerFontManager>()

    private val screenCaptureProcessor by inject<ScreenCaptureProcessor>()

    private val appcuesCoroutineScope by inject<AppcuesCoroutineScope>()

    private val experienceRenderer by inject<ExperienceRenderer>()

    private val logMessageManager by inject<DebuggerLogMessageManager>()

    sealed class UIState(val mode: DebugMode) {
        class Creating(mode: DebugMode) : UIState(mode)
        class Idle(mode: DebugMode) : UIState(mode)
        class Dragging(mode: DebugMode, val dragAmount: Offset) : UIState(mode)
        class Expanded(mode: DebugMode) : UIState(mode)
        class Dismissing(mode: DebugMode) : UIState(mode)
        class Dismissed(mode: DebugMode) : UIState(mode)
    }

    sealed class ToastState {
        object Idle : ToastState()
        data class Rendering(val type: DebuggerToast) : ToastState()
    }

    private val _uiState = MutableStateFlow<UIState>(Creating(debugMode))

    val uiState: StateFlow<UIState>
        get() = _uiState

    private val _deeplink = MutableStateFlow<String?>(null)

    val deeplink: StateFlow<String?>
        get() = _deeplink

    private val _toastState = MutableStateFlow<ToastState>(ToastState.Idle)

    val toastState: StateFlow<ToastState>
        get() = _toastState

    private val _statusInfo = MutableStateFlow<List<DebuggerStatusItem>>(arrayListOf())

    val statusInfo: StateFlow<List<DebuggerStatusItem>>
        get() = _statusInfo

    private val _logMessages = MutableStateFlow<List<LogMessage>>(arrayListOf())

    val logMessages: StateFlow<List<LogMessage>>
        get() = _logMessages

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

    init {
        debuggerStatusManager.start()
        debuggerRecentEventsManager.start()
        logMessageManager.start()

        with(viewModelScope) {
            launch { debuggerRecentEventsManager.data.collect { _events.value = it } }
            launch { debuggerStatusManager.data.collect { _statusInfo.value = it } }
            launch { logMessageManager.data.collect { _logMessages.value = it } }
        }
    }

    fun onStart(mode: DebugMode, deeplink: String?) {
        _deeplink.value = deeplink
        if (!deeplink.isNullOrEmpty()) {
            // the deep link might be from the debugger checking that the configuration is correct
            // pass along to allow it to check
            viewModelScope.launch {
                if (!debuggerStatusManager.checkDeepLinkValidation(deeplink)) {
                    when (_uiState.value) {
                        // if currently idle and a new link comes in - expand to that link
                        is Idle -> transition(Expanded(mode))
                        // currently open - reset to the new path
                        is Expanded -> transition(Expanded(mode))
                        // otherwise, no valid link action available
                        else -> Unit
                    }
                }
            }
        }
    }

    fun onRender() {
        when (val currentMode = uiState.value.mode) {
            is Debugger -> {
                // if we had a deep link from initial startup, process it now
                _uiState.value = if (_deeplink.value.isNullOrEmpty()) Idle(currentMode) else Expanded(currentMode)
            }
            is ScreenCapture -> {
                _uiState.value = Idle(currentMode)
            }
        }
    }

    fun onDismissAnimationCompleted() {
        if (_uiState.value is Dismissing) {
            _uiState.value = Dismissed(uiState.value.mode)
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
                _uiState.value = Idle(state.mode)
            }
            else -> Unit
        }
    }

    fun closeExpandedView() {
        if (_uiState.value is Expanded) {
            _uiState.value = Idle(uiState.value.mode)
        }
    }

    fun captureScreen(debuggerState: MutableDebuggerState) {
        appcuesCoroutineScope.launch {
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
        _uiState.value = Idle(uiState.value.mode)

        when (val currentMode = uiState.value.mode) {
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
        debuggerStatusManager.reset()
        debuggerRecentEventsManager.reset()
        logMessageManager.reset()
    }

    private fun List<DebuggerEventItem>.hideEventsForFab(): List<DebuggerEventItem> {
        return toMutableList().onEach { it.showOnFab = false }
    }

    fun consumeDeeplink() {
        _deeplink.value = null
    }
}
