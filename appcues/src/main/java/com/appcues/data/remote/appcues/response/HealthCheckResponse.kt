package com.appcues.data.remote.appcues.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class HealthCheckResponse(
    val ok: Boolean
)
