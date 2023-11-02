package com.appcues.data.remote.customerapi.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class InsetsRequest(
    val left: Int,
    val right: Int,
    val top: Int,
    val bottom: Int,
)
