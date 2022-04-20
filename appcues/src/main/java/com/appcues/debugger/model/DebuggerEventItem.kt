package com.appcues.debugger.model

internal data class DebuggerEventItem(
    val name: String,
    val type: EventType,
)

internal enum class EventType {
    EXPERIENCE, GROUP, USER_PROFILE, CUSTOM, SCREEN
}
