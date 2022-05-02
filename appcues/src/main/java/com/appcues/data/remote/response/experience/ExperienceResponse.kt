package com.appcues.data.remote.response.experience

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ExperienceResponse(
    val id: UUID,
    val name: String,
    val theme: String?,
    val actions: Map<UUID, List<ActionResponse>>?,
    val traits: List<TraitResponse>,
    val steps: List<StepContainerResponse>,
    val state: String?,
)
