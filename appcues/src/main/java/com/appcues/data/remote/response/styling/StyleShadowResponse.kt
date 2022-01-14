package com.appcues.data.remote.response.styling

internal data class StyleShadowResponse(
    val color: StyleColorResponse,
    val radius: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
)
