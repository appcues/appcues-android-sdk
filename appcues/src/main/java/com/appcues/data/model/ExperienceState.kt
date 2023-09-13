package com.appcues.data.model

internal data class ExperienceState(
    val experience: Experience,
    val stepIndex: Int,
) {

    val step = experience.flatSteps[stepIndex]
}
