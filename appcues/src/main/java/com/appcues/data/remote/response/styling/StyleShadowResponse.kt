package com.appcues.data.remote.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleShadowResponse(
    val color: StyleColorResponse,
    val radius: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
)
