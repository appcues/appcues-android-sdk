package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.ExperienceResponse

internal data class QualifyResponse(
    val experiences: List<ExperienceResponse>,
    val performedQualification: Boolean
)
