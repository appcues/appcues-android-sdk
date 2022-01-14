package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.data.remote.response.styling.StyleResponse

internal data class StepItemContentResponse(
    val type: String,
    val id: String,
    val imageUrl: String? = null,
    val contentMode: String? = null,
    val intrinsicSize: SizeResponse? = null,
    val style: StyleResponse? = null,
    val text: String? = null,
    val content: StepItemContentResponse? = null,
)
