package com.appcues.domain

import com.appcues.domain.gateway.DataGateway

internal class ShowUseCase(
    private val data: DataGateway,
    private val showExperienceUseCase: ShowExperienceUseCase,
) {

    suspend operator fun invoke(contentId: String) {
        showExperienceUseCase(data.getContent(contentId))
    }
}
