package com.appcues.data.remote.appcues.response.rules

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ConditionResponse(
    val and: List<ConditionResponse>?,
    val or: List<ConditionResponse>?,
    val nor: List<ConditionResponse>?,
    val not: ConditionResponse?,
    val screen: ScreenConditionResponse?,
    val trigger: TriggerConditionResponse?,
    val attributes: AttributesConditionResponse?,
    val properties: PropertiesConditionResponse?,
)
