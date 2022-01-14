package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.google.gson.annotations.SerializedName

internal data class TacoResponse(
    val checklists: List<Any>,
    val contents: List<Any>,
    val experiences: List<ExperienceResponse>,
    val profile: ProfileResponse,

    @SerializedName("performed_qualification")
    val performedQualifications: Boolean,
    @SerializedName("qualification_reason")
    val qualificationReason: String,
    @SerializedName("request_id")
    val requestId: String
)
