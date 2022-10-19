package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QualifyResponse(
    val experiences: List<ExperienceResponse>,

    val experiments: Map<String, ExperimentResponse>?,

    @Json(name = "performed_qualification")
    val performedQualification: Boolean,

    @Json(name = "qualification_reason")
    val qualificationReason: String? // screen_view, event_trigger, forced
)
