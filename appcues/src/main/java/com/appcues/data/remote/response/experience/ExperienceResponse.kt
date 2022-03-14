package com.appcues.data.remote.response.experience

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.response.trait.TraitResponse
import java.util.UUID

internal data class ExperienceResponse(
    val id: UUID,
    val name: String,
    val theme: String?,
    val actions: HashMap<UUID, List<ActionResponse>>?,
    val traits: List<TraitResponse>,
    val steps: List<StepContainerResponse>
)
