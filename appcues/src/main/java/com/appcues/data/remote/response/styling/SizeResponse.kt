package com.appcues.data.remote.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SizeResponse(
    val width: Int,
    val height: Int,
)
