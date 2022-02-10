package com.appcues.domain

import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.CustomerExperienceGateway

internal class ShowExperienceUseCase(
    private val customerExperience: CustomerExperienceGateway
) {

    suspend operator fun invoke(experience: Experience) {
        // In the future we could have the state machine for given experience here.
        return customerExperience.showExperience(experience)
    }
}
