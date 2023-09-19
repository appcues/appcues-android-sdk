package com.appcues.data.remote.appcues.response.experience

import com.appcues.data.remote.appcues.response.step.StepContainerResponse
import com.appcues.data.remote.appcues.response.trait.TraitResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ExperienceResponse(
    val id: UUID,
    val name: String,
    val theme: String?,
    val traits: List<TraitResponse>,
    val steps: List<StepContainerResponse>,
    val state: String?,
    val type: String?,
    val publishedAt: Long?,
    val nextContentId: String?,
    val redirectUrl: String?,
    val context: ContextResponse?,
) : LossyExperienceResponse()

@JsonClass(generateAdapter = true)
internal data class FailedExperienceResponse(
    val id: UUID,
    val name: String?,
    val type: String?,
    val publishedAt: Long?,
    val context: ContextResponse?,
    var error: String? = null
) : LossyExperienceResponse()

internal sealed class LossyExperienceResponse

@JsonClass(generateAdapter = true)
internal data class ContextResponse(
    // this value should be a UUID, but it is just for analytics, so capture and return string verbatim
    @Json(name = "locale_id")
    val localeId: String?,
    @Json(name = "locale_name")
    val localeName: String?,
)
