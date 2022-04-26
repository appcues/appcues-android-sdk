package com.appcues.debugger.model

internal data class DebuggerEventItem(
    val type: EventType,
    val title: String,
    val timestamp: Long,
    val name: String,
    val properties: List<Pair<String, Any>>?,
    val identityProperties: List<Pair<String, Any>>?
)

internal enum class EventType {
    EXPERIENCE, GROUP_UPDATE, USER_PROFILE, CUSTOM, SCREEN, SESSION
}
