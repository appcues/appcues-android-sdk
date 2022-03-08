package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.ExperienceResponse

internal data class ActivityResponse(
    val experiences: List<ExperienceResponse>,
    val performedQualification: Boolean
)
