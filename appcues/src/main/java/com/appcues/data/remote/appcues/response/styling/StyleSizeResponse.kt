package com.appcues.data.remote.appcues.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleSizeResponse(
    val width: Double,
    val height: Double,
)
