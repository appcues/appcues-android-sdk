package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.data.remote.response.styling.StyleResponse
import com.google.gson.Gson
import java.util.UUID

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
) {

    companion object {

        /**
         * Try to create a [StyleResponse] from [Any]
         */
        fun fromAny(any: Any?): StepContentResponse? {
            return Gson().run {
                fromJson(toJsonTree(any).asJsonObject, StepContentResponse::class.java)
            }
        }
    }
}
