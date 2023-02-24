package com.appcues.data.remote.appcues.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleBackgroundImageResponse(
    val imageUrl: String,
    val blurHash: String? = null,
    val intrinsicSize: StyleSizeResponse? = null,
    val contentMode: String? = null,
    val verticalAlignment: String? = null,
    val horizontalAlignment: String? = null,
)
