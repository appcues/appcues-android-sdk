package com.appcues.data.remote.appcues.response.rules

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RulesResponse(
    val conditions: ConditionResponse,
    // other props
    val updatedAt: Long,
    val frequency: String? = null
)
