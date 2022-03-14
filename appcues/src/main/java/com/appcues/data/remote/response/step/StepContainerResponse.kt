package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.trait.TraitResponse
import java.util.UUID

internal data class StepContainerResponse(
    val id: UUID,
    val children: List<StepResponse>,
    val traits: List<TraitResponse>,
    val actions: HashMap<UUID, List<ActionResponse>>
)
