package com.appcues.data.remote.appcues.request

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class EventRequest(
    val name: String,
    val timestamp: Date = Date(),
    val attributes: MutableMap<String, Any> = hashMapOf(),
    val context: MutableMap<String, Any> = hashMapOf()
)
