package com.appcues.data.remote.appcues.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ExperimentResponse(
    val group: String,
    @Json(name = "experiment_id")
    val experimentId: UUID,
    @Json(name = "experience_id")
    val experienceId: UUID,
    @Json(name = "goal_id")
    val goalId: String,
    @Json(name = "content_type")
    val contentType: String,
)
