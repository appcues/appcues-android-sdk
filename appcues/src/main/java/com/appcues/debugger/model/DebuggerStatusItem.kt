package com.appcues.debugger.model

internal data class DebuggerStatusItem(
    val title: String,
    val statusType: StatusType,
    val line1: String? = null,
    val line2: String? = null,
    val showRefreshIcon: Boolean = false,
    val tapActionType: TapActionType? = null
)

internal enum class StatusType {
    PHONE, LOADING, SUCCESS, ERROR, EXPERIENCE, IDLE
}

internal enum class TapActionType {
    HEALTH_CHECK,
    DEEPLINK_CHECK,
    PUSH_CHECK
}
