package com.appcues.data.remote.appcues.response.rules

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PropertiesConditionResponse(
    val property: String,
    val operator: String,
    val value: String?,
)
