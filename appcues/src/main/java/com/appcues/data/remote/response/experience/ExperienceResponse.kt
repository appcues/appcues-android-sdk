package com.appcues.data.remote.response.experience

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse
import java.util.UUID

internal data class ExperienceResponse(
    val id: UUID,
    val name: String,
    val tags: List<Any>,
    val theme: ExperienceThemeResponse,
    val actions: HashMap<String, ActionResponse>,
    val traits: List<TraitResponse>,
    val steps: List<StepResponse>
)
