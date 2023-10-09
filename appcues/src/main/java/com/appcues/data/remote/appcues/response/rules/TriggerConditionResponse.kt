package com.appcues.data.remote.appcues.response.rules

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TriggerConditionResponse(
    val event: String,
    val conditions: ConditionResponse?,
)
