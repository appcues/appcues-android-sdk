package com.appcues.data.remote.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleGradientColorResponse(
    val colors: List<StyleColorResponse>,
    val startPoint: String,
    val endPoint: String,
)
