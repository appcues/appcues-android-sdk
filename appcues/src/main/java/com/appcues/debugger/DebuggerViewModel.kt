package com.appcues.debugger

import android.app.Activity
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.AppcuesConfig
import com.appcues.R
import com.appcues.analytics.AnalyticsTracker
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebugMode.ScreenCapture
import com.appcues.debugger.DebuggerViewModel.UIState.Creating
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissed
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.debugger.DebuggerViewModel.UIState.Idle
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.model.DebuggerFontItem
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.EventType
import com.appcues.debugger.model.TapActionType
import com.appcues.debugger.screencapture.Capture
import com.appcues.debugger.screencapture.asCaptureView
import com.appcues.debugger.screencapture.generateCaptureMetadata
import com.appcues.debugger.screencapture.prettyPrint
import com.appcues.debugger.screencapture.screenCaptureDisplayName
import com.appcues.debugger.screencapture.screenshot
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.util.ContextResources
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import java.util.Date

internal class DebuggerViewModel(
    override val scope: Scope,
) : ViewModel(), KoinScopeComponent {

    private val analyticsTracker by inject<AnalyticsTracker>()

    private val debuggerStatusManager by inject<DebuggerStatusManager>()

    private val debuggerRecentEventsManager by inject<DebuggerRecentEventsManager>()

    private val debuggerFontManager by inject<DebuggerFontManager>()

    private val contextResources by inject<ContextResources>()

    private val config by inject<AppcuesConfig>()

    sealed class UIState {
        object Creating : UIState()
        data class Idle(val mode: DebugMode) : UIState()
        data class Dragging(val dragAmount: Offset) : UIState()
        data class Expanded(val mode: DebugMode) : UIState()
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
                        (_uiState.value is Idle && reset)) {
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
                    (_uiState.value is Idle && reset)) {
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

    fun onScreenCaptureConfirm(capture: Capture) {
        _uiState.value = Idle(ScreenCapture)

        // upcoming work to execute API calls starts here

        // TESTING!!
        capture.prettyPrint()
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

    fun captureScreen(): Capture? =
        AppcuesActivityMonitor.activity?.captureScreen(config, contextResources)
}

private fun Activity.captureScreen(
    config: AppcuesConfig,
    contextResources: ContextResources,
): Capture? =
    window.decorView.rootView.let {
        // hide the debugger view for screen capture, if present
        val debuggerView = it.findViewById<View>(R.id.appcues_debugger_view)
        debuggerView?.let { view ->
            view.visibility = View.GONE
        }

        val timestamp = Date()
        val displayName = it.screenCaptureDisplayName(timestamp)
        val screenshot = it.screenshot()
        val layout = it.asCaptureView()
        val capture = if (screenshot != null && layout != null) {
            Capture(
                appId = config.applicationId,
                displayName = displayName,
                screenshotImageUrl = null,
                layout = layout,
                metadata = contextResources.generateCaptureMetadata(),
                timestamp = timestamp,
            ).apply {
                this.screenshot = screenshot
            }
        } else null

        // restore debugger view visibility, if present
        debuggerView?.let { view ->
            view.visibility = View.VISIBLE
        }

        capture
    }
