package com.appcues.debugger.model

import kotlin.random.Random

internal data class DebuggerEventItem(
    val id: Int,
    val type: EventType,
    val timestamp: Long,
    val name: String,
    val properties: List<Pair<String, Any>>?,
    val identityProperties: List<Pair<String, Any>>?,
    var showOnFab: Boolean = true,
) {

    // overriding equals and hashCode allows StateFlow to emit the
    // collection even if elements are the same. useful when we change property showOnFab to false
    // and try to emit the collection again
    @Suppress("EqualsAlwaysReturnsTrueOrFalse")
    override fun equals(other: Any?) = false

    override fun hashCode() = Random.Default.nextInt()
}

internal enum class EventType {
    EXPERIENCE, GROUP_UPDATE, USER_PROFILE, CUSTOM, SCREEN, SESSION
}
