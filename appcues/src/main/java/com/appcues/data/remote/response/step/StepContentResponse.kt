package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.styling.StyleResponse

internal data class StepContentResponse(
    val id: String,
    val type: String,
    val orientation: String? = null,
    val style: StyleResponse? = null,
    val items: List<StepItemResponse>? = null,
)
