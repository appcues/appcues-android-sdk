package com.appcues.debugger.model

internal sealed class DebuggerToast {
    data class ScreenCaptureSuccess(val displayName: String, val onDismiss: () -> Unit) : DebuggerToast()
    data class ScreenCaptureFailure(val onDismiss: () -> Unit, val onRetry: () -> Unit, val error: CaptureError? = null) : DebuggerToast() {
        enum class CaptureError {
            INVALID_TOKEN
        }
    }
}
