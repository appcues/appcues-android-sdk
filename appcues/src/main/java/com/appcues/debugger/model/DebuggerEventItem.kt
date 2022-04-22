package com.appcues.debugger.model

internal data class DebuggerEventItem(
    val type: EventType,
    val title: String,
    val timestamp: String,
    val properties: HashMap<String, Any>?,
)

internal enum class EventType {
    EXPERIENCE, GROUP_UPDATE, USER_PROFILE, CUSTOM, SCREEN, SESSION
}
