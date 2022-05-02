package com.appcues.data.remote.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleColorResponse(
    val light: String,
    val dark: String? = null,
)
