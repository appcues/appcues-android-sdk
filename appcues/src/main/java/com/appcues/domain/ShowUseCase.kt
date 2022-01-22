package com.appcues.domain

import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.domain.gateway.ExperienceLocalGateway
import com.appcues.domain.gateway.ExperienceRemoteGateway

internal class ShowUseCase(
    private val experienceRemote: ExperienceRemoteGateway,
    private val experienceLocal: ExperienceLocalGateway,
    private val customerView: CustomerViewGateway
) {

    suspend operator fun invoke(contentId: String): Boolean {
        return experienceRemote.getExperience(contentId)?.let {
            experienceLocal.saveExperience(it)
            customerView.showExperience(it.id)
            true
        } ?: false
    }
}
