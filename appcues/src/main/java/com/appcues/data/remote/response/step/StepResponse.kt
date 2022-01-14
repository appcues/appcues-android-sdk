package com.appcues.data.remote.response.step

import com.appcues.data.remote.response.trait.TraitResponse

internal data class StepResponse(
    val id: String,
    val contentType: String,
    val content: StepContentResponse,
    val traits: List<TraitResponse>,
    val actions: HashMap<String, List<StepActionResponse>>
)
