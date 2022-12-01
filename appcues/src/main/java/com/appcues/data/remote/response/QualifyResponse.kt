package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.LossyExperienceResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QualifyResponse(
    val experiences: List<LossyExperienceResponse>,

    val experiments: List<ExperimentResponse>?,

    @Json(name = "performed_qualification")
    val performedQualification: Boolean,

    @Json(name = "qualification_reason")
    val qualificationReason: String? // screen_view, event_trigger, forced
)
