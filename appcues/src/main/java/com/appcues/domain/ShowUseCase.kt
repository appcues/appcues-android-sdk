package com.appcues.domain

import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.domain.gateway.ExperienceGateway

internal class ShowUseCase(
    private val experienceGateway: ExperienceGateway,
    private val customerViewGateway: CustomerViewGateway
) {

    suspend operator fun invoke(contentId: String) {
        with(experienceGateway.getExperiences(contentId)) {
            customerViewGateway.showExperiences(this)
        }
    }
}
