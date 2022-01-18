package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.data.remote.response.styling.StyleResponse
import java.util.UUID

internal data class StepContentResponse(
    val id: UUID,
    val type: String,
    val orientation: String? = null,
    val style: StyleResponse? = null,
    val items: List<StepItemResponse>? = null,
    val imageUrl: String? = null,
    val contentMode: String? = null,
    val intrinsicSize: SizeResponse? = null,
    val text: String? = null,
    val content: StepContentResponse? = null,
)
