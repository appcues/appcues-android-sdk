package com.appcues.data.remote.request

import java.util.Date

internal data class EventRequest(
    val name: String,
    val timestamp: Date = Date(),
    val attributes: HashMap<String, Any> = hashMapOf(),
    val context: HashMap<String, Any> = hashMapOf()
)
