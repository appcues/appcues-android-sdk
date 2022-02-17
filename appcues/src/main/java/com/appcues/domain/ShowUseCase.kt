package com.appcues.domain

import com.appcues.domain.gateway.DataGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ShowUseCase(
    private val data: DataGateway,
    private val showExperienceUseCase: ShowExperienceUseCase,
) {

    suspend operator fun invoke(contentId: String) = withContext(Dispatchers.IO) {
        showExperienceUseCase(data.getContent(contentId))
    }
}
