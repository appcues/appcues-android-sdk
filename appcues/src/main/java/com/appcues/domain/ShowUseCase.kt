package com.appcues.domain

import com.appcues.domain.gateway.ExperienceRemoteGateway

internal class ShowUseCase(
    private val experienceRemote: ExperienceRemoteGateway,
    private val showExperienceUseCase: ShowExperienceUseCase,
) {

    suspend operator fun invoke(contentId: String) {
        experienceRemote.getExperience(contentId)?.let {
            showExperienceUseCase(it)
        }
    }
}
