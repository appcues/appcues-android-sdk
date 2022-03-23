package com.appcues.data.remote.response

import com.google.gson.annotations.SerializedName

internal data class ErrorResponse(
    val error: Boolean,
    @SerializedName("status_code")
    val statusCode: Int,
    val message: ErrorMessageResponse
)

internal data class ErrorMessageResponse(
    val description: String,
    val title: String,
    val type: String,
)
