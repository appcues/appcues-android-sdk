package com.appcues.data.remote.appcues.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleShadowResponse(
    val color: StyleColorResponse,
    val radius: Double = 0.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
)
