package com.appcues.data.remote.response.step

import java.util.UUID

internal data class StepItemResponse(
    val id: UUID,
    val type: String,
    val orientation: String? = null,
    val distribution: String? = null,
    val blockType: String? = null,
    val items: List<StepItemResponse>? = null,
    val content: StepContentResponse? = null,
)
