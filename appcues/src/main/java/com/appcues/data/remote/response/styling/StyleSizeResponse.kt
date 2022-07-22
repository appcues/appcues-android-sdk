package com.appcues.data.remote.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleSizeResponse(
    val width: Int,
    val height: Int,
)
