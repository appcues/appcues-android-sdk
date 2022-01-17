package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.data.remote.response.styling.StyleResponse
import java.util.UUID

internal data class StepItemContentResponse(
    val id: UUID,
    val type: String,
    val imageUrl: String? = null,
    val contentMode: String? = null,
    val intrinsicSize: SizeResponse? = null,
    val style: StyleResponse? = null,
    val text: String? = null,
    val content: StepItemContentResponse? = null,
)
