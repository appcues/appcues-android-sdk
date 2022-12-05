package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class StepResponse(
    val id: UUID,
    val content: PrimitiveResponse,
    val traits: List<TraitResponse>,
    val actions: Map<UUID, List<ActionResponse>>,
    val type: String,
)
