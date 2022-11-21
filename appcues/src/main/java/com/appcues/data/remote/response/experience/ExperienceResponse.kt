package com.appcues.data.remote.response.experience

import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.response.trait.TraitResponse
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
) : LossyExperienceResponse()

@JsonClass(generateAdapter = true)
internal data class FailedExperienceResponse(
    val id: UUID,
    val name: String?,
    val type: String?,
    val publishedAt: Long?,
    var error: String? = null
) : LossyExperienceResponse()

internal sealed class LossyExperienceResponse
