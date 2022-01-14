package com.appcues.data.remote.response.experience

import com.appcues.data.remote.response.step.StepResponse

internal data class ExperienceResponse(
    val id: String,
    val name: String,
    val tags: List<Any>,
    val theme: ExperienceThemeResponse,
    val actions: HashMap<String, ExperienceActionResponse>,
    val traits: List<ExperienceTraitResponse>,
    val steps: List<StepResponse>
)
