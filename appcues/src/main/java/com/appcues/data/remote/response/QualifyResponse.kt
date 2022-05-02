package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QualifyResponse(
    val experiences: List<ExperienceResponse>,

    @Json(name = "performed_qualification")
    val performedQualification: Boolean
)
