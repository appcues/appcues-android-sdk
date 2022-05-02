package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.data.remote.response.styling.StyleResponse
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class StepContentResponse(
    val id: UUID,
    val type: String,
    val orientation: String? = null,
    val style: StyleResponse? = null,
    val items: List<StepContentResponse>? = null,
    val imageUrl: String? = null,
    val embed: String? = null,
    val contentMode: String? = null,
    val blurHash: String? = null,
    val intrinsicSize: SizeResponse? = null,
    val accessibilityLabel: String? = null,
    val text: String? = null,
    val content: StepContentResponse? = null,
    val distribution: String? = null,
    val spacing: Int = 0,
)
