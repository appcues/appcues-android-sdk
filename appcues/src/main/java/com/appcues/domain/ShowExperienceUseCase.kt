package com.appcues.domain

import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.CustomerViewGateway

internal class ShowExperienceUseCase(
    private val customerView: CustomerViewGateway
) {

    suspend operator fun invoke(experience: Experience) {
        // In the future we could have the state machine for given experience here.
        return customerView.showExperience(experience)
    }
}
