package com.appcues.data.remote.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ActivityResponse(
    val ok: Boolean
)
