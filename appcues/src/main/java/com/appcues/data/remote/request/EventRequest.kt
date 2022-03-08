package com.appcues.data.remote.request

import com.appcues.data.remote.UnixTimestampAdapter
import com.google.gson.annotations.JsonAdapter
import java.util.Date

internal data class EventRequest(
    val name: String,
    @JsonAdapter(UnixTimestampAdapter::class)
    val timestamp: Date = Date(),
    val attributes: HashMap<String, Any> = hashMapOf(),
    val context: HashMap<String, Any> = hashMapOf()
)
