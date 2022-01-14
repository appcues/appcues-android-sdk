package com.appcues.data.remote.response.styling

internal data class StyleGradientColorResponse(
    val colors: List<StyleColorResponse>,
    val startPoint: String,
    val endPoint: String,
)
