package com.appcues.data.remote.sdksettings.response

import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.data.remote.appcues.response.rules.RulesResponse
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QualificationsResponse(
    val rule: RulesResponse,
    val experience: ExperienceResponse,
    val sortPriority: Int,
)
