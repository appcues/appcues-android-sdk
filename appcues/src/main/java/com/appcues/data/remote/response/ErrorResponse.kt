package com.appcues.data.remote.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ErrorResponse(
    val error: Boolean,
    @Json(name = "status_code")
    val statusCode: Int,
    val message: ErrorMessageResponse
)

@JsonClass(generateAdapter = true)
internal data class ErrorMessageResponse(
    val description: String,
    val title: String,
    val type: String,
)
