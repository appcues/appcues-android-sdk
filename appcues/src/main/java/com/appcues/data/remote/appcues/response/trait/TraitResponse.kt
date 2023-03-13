package com.appcues.data.remote.appcues.response.trait

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TraitResponse(
    val type: String,
    val config: Map<String, Any>? = null,
)
