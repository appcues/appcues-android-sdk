package com.appcues.data.remote.response.step

internal data class StepItemResponse(
    val id: String,
    val type: String,
    val orientation: String? = null,
    val distribution: String? = null,
    val blockType: String? = null,
    val items: List<StepItemResponse>? = null,
    val content: StepItemContentResponse? = null,
)
