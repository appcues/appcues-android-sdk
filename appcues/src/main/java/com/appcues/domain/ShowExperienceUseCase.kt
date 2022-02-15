package com.appcues.domain

import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.ExperienceGateway

internal class ShowExperienceUseCase(
    private val experienceGateway: ExperienceGateway
) {

    suspend operator fun invoke(experience: Experience) {
        return experienceGateway.start(experience)
    }
}
