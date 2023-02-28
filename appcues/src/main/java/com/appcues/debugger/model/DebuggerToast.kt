package com.appcues.debugger.model

import com.appcues.debugger.screencapture.Capture

internal sealed class DebuggerToast {
    data class ScreenCaptureSuccess(val capture: Capture, val onDismiss: () -> Unit) : DebuggerToast()
    data class ScreenCaptureFailure(val capture: Capture, val onDismiss: () -> Unit, val onRetry: () -> Unit) : DebuggerToast()
}
