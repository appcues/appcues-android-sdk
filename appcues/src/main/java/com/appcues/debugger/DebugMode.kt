package com.appcues.debugger

internal sealed class DebugMode {
    data class Debugger(val deepLinkPath: String?) : DebugMode()
    object ScreenCapture : DebugMode()
}
