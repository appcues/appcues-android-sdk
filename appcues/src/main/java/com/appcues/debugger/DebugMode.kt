package com.appcues.debugger

internal sealed class DebugMode {
    object Debugger : DebugMode()
    data class ScreenCapture(val token: String) : DebugMode()
}
