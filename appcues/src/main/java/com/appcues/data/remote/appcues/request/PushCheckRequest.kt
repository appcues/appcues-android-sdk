package com.appcues.data.remote.appcues.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PushCheckRequest(
    @Json(name = "device_id")
    val deviceId: String,
    @Json(name = "test_id")
    val token: String
)
