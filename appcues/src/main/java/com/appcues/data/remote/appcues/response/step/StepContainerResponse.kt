package com.appcues.data.remote.appcues.response.step

import com.appcues.data.remote.appcues.response.action.ActionResponse
import com.appcues.data.remote.appcues.response.trait.TraitResponse
import java.util.UUID

internal data class StepContainerResponse(
    val id: UUID,
    val children: List<StepResponse>,
    val traits: List<TraitResponse>,
    val actions: Map<UUID, List<ActionResponse>>
)
