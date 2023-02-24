package com.appcues.data.remote.appcues.response.action

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ActionResponse(
    val on: String,
    val type: String,
    val config: Map<String, Any>?,
)
