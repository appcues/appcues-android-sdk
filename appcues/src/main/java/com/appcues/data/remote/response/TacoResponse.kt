package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.google.gson.annotations.SerializedName
import java.util.UUID

internal data class TacoResponse(
    @SerializedName("experiences")
    val experiences: List<ExperienceResponse>,
    @SerializedName("performed_qualification")
    val performedQualifications: Boolean,
    @SerializedName("qualification_reason")
    val qualificationReason: String,
    @SerializedName("request_id")
    val requestId: UUID
)
