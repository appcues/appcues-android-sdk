package com.appcues.data.model

import com.appcues.data.model.ExperienceTrigger.Qualification

internal data class QualificationResult(
    val trigger: Qualification,
    val experiences: List<Experience>
)
