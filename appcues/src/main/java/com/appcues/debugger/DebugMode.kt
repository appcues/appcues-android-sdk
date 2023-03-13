package com.appcues.debugger

internal sealed class DebugMode {
    data class Debugger(val deepLinkPath: String?) : DebugMode()
    data class ScreenCapture(val token: String) : DebugMode()
}
